package money.terra.terrawallet.test;

import jdk.vm.ci.code.site.Call;
import money.terra.terrawallet.TerraWalletSDK;
import money.terra.terrawallet.WalletModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class TerraWalletTest {

    @Test
    public void walletGenerateAndRecoverTest() throws Exception {
        final int count = 100;

        ArrayList<String[]> wallets = new ArrayList();
        // generate
        for (int i = 0; i < count; i++) {
            String[] wallet = TerraWalletSDK.getNewWallet();

            String privateKey = wallet[0];
            String publicKey = wallet[1];
            String publicKey64 = wallet[2];
            String address = wallet[3];
            String mnemonic = wallet[4];

            Assert.assertEquals("generated[" + i + "], privateKey is wrong.", 64, privateKey.length());
            Assert.assertEquals("generated[" + i + "], publicKey32 is wrong." + publicKey, 66, publicKey.length());
            Assert.assertEquals("generated[" + i + "], publicKey64 is wrong." + publicKey64, 130, publicKey64.length());
            Assert.assertEquals("generated[" + i + "], mnemonic is wrong.", 24, mnemonic.split(" ").length);



            //success generating.
            wallets.add(wallet);
        }

        // recover from generated above.

        Assert.assertEquals("items generated not enough.", wallets.size(), count);

        for (int i = 0; i < wallets.size(); i++) {

            String mnemonic = wallets.get(i)[4];
            String[] wallet = TerraWalletSDK.getNewWalletFromSeed(mnemonic, 330);

            String privateKey = wallet[0];
            String publicKey = wallet[1];   //compressed public key.
            String publicKey64 = wallet[2];   //'un'compressed public key.
            String address = wallet[3];

            //check privateKey(recovered and generated is equal)
            Assert.assertEquals("recovered[" + i + "], privateKey is wrong.", wallets.get(i)[0], privateKey);
            //check publicKey(recovered and generated is equal)
            Assert.assertEquals("recovered[" + i + "], publicKey is wrong.", wallets.get(i)[1], publicKey);
            //check address(recovered and generated is equal)
            Assert.assertEquals("recovered[" + i + "], address is wrong.", wallets.get(i)[3], address);
        }

    }

    @Test
    public void transferTest() {
        String[] wallet = TerraWalletSDK.getNewWalletFromSeed("airport fox tomorrow arm slab invest size bird eyebrow push swarm fork bone grant ketchup wear pepper manual apart brand thank trash advance burger", 330);
        String hexPrivateKey = wallet[0];
        String hexPublicKey = wallet[1];
        String address = wallet[3];

        String chainId = "soju-0014";
        String toAddress = "terra1y56xnxa2aaxtuc3rpntgxx0qchyzy2wp7dqgy3";
        String transferBalance = "1000000"; //1 Luna

        JSONObject accountInfo = getAccountInfo(address);
        String accountNumber = accountInfo.getString("account_number");
        String sequence = accountInfo.getString("sequence");
        int luna = accountInfo.getInt("luna");

        Assert.assertEquals("not enough luna balance.", true, (luna >= 1000));
        Assert.assertEquals("account number is not valid.", true, (!accountNumber.equals("")));
        Assert.assertEquals("sequence is not valid.", true, (!sequence.equals("")));
//
        JSONObject message = makeTransferMessage(address, toAddress, accountNumber, chainId,
            sequence, transferBalance);

        Assert.assertEquals("makeTransferMessage is wrong", true, !message.isEmpty());

        JSONObject signed = null;
        try {
            signed = TerraWalletSDK.sign(message, sequence, accountNumber, chainId, hexPrivateKey, hexPublicKey);
        }catch(Exception e) {
            Assert.fail("signed message is wrong.");
        }

        JSONObject response = broadcast(signed);
        System.out.println("response : " + response.toString());
    }

    @Test
    public void marketSwapTest() {
        String[] wallet = TerraWalletSDK.getNewWalletFromSeed("airport fox tomorrow arm slab invest size bird eyebrow push swarm fork bone grant ketchup wear pepper manual apart brand thank trash advance burger", 330);
        String hexPrivateKey = wallet[0];
        String hexPublicKey = wallet[1];
        String address = wallet[3];

        String chainId = "soju-0014";
        String toCurrency = "usdr"; //SDT
        String swapBalance = "1000000"; //1Luna

        JSONObject accountInfo = getAccountInfo(address);
        String accountNumber = accountInfo.getString("account_number");
        String sequence = accountInfo.getString("sequence");
        int luna = accountInfo.getInt("luna");

        Assert.assertEquals("not enough luna balance.", true, (luna >= 1000));
        Assert.assertEquals("account number is not valid.", true, (!accountNumber.equals("")));
        Assert.assertEquals("sequence is not valid.", true, (!sequence.equals("")));
//
        JSONObject message = makeMarketSwapMessage(address, toCurrency, accountNumber, chainId,
                sequence, swapBalance);

        Assert.assertEquals("makeMarketSwapMessage is wrong", true, !message.isEmpty());

        JSONObject signed = null;
        try {
            signed = TerraWalletSDK.sign(message, sequence, accountNumber, chainId, hexPrivateKey, hexPublicKey);
        }catch(Exception e) {
            Assert.fail("signed message is wrong.");
        }

        JSONObject response = broadcast(signed);
        System.out.println("response : " + response.toString()) ;
    }

    private JSONObject getAccountInfo(String address) {
        String accountNumber = "";
        String sequence = "";
        int luna = 0;

        try {
            JSONObject accountInfo = httpConnection("/auth/accounts/" + address, true);
            JSONObject value = accountInfo.getJSONObject("result").getJSONObject("value");
            JSONArray coins = value.getJSONArray("coins");

            accountNumber = value.getString("account_number");
            sequence = value.getString("sequence");
            luna = 0;
            for (int i = 0; i < coins.length(); i++) {
                JSONObject coin = coins.getJSONObject(i);
                if (coin.getString("denom").equals("uluna")) {
                    luna = Integer.parseInt(coin.getString("amount"));
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }finally {
            JSONObject result = new JSONObject();
            result.put("account_number", accountNumber);
            result.put("sequence", sequence);
            result.put("luna", luna);
            return result;
        }
    }

    private JSONObject makeTransferMessage(String from, String to, String accountNumber, String chainId, String sequence, String transferBalance) {

        JSONObject requestJson = new JSONObject("{\"base_req\": {\"from\": \"" + from +
                "\",\"memo\": \"Sent via terra-wallet-java test\",\"chain_id\": \"" + chainId +
                "\",\"account_number\": \"" + accountNumber +
                "\",\"sequence\": \"" + sequence +
                "\",\"gas\": \"200000\",\"gas_adjustment\": \"1.2\",\"fees\": [{\"denom\": \"uluna\",\"amount\": \"50\"}],\"simulate\": false},\"coins\": [{\"denom\": \"uluna\",\"amount\": \"" + transferBalance + "\"}]}");

        try {
            JSONObject response = httpConnection("/bank/accounts/" + to + "/transfers", false, requestJson);
            return response.getJSONObject("value");
        }catch(Exception e) {
            return new JSONObject();
        }
    }

    private JSONObject makeMarketSwapMessage(String from, String toCurrency, String accountNumber, String chainId, String sequence, String swapBalance){
        JSONObject requestJson = new JSONObject("{\"base_req\": {\"from\": \"" + from +
                "\",\"memo\": \"Sent via terra-wallet-java test\",\"chain_id\": \"" + chainId +
                "\",\"account_number\": \"" + accountNumber +
                "\",\"sequence\": \"" + sequence +
                "\",\"gas\": \"200000\",\"gas_adjustment\": \"1.2\",\"fees\": [{\"denom\": \"uluna\",\"amount\": \"50\"}],\"simulate\": false},\"offer_coin\": {\"denom\": \"uluna\",\"amount\": \"" + swapBalance +
                "\"},\"ask_denom\": \"" + toCurrency + "\"}");

        try {
            JSONObject response = httpConnection("/market/swap", false, requestJson);
            return response.getJSONObject("value");
        }catch(Exception e) {
            return new JSONObject();
        }
    }

    private JSONObject broadcast(JSONObject message) {
        return httpConnection("/txs", false, message);
    }

    public JSONObject httpConnection(String targetUrl, boolean isGet) {
        return httpConnection(targetUrl, isGet, null);
    }

    public JSONObject httpConnection(String targetUrl, boolean isGet, JSONObject params) {

        BufferedReader br = null;
        try {
            URL url = new URL("https://soju-fcd.terra.dev" + targetUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod(isGet ? "GET" : "POST");

            if (params != null) {
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();

                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(params.toString());

                writer.flush();
                writer.close();
                os.close();
            }

            conn.connect();

            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuffer sb = new StringBuffer();
            String jsonData = "";
            while ((jsonData = br.readLine()) != null) {
                sb.append(jsonData);
            }

            return new JSONObject(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new JSONObject();
    }
}
