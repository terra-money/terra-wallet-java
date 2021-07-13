package money.terra.terrawallet;

import com.google.gson.Gson;

import money.terra.terrawallet.library.Base64;
import money.terra.terrawallet.library.Sha256;
import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

public class Sign {

    private byte[] privateKey;
    private byte[] publicKey;
    private String sequence;
    private String accountNumber;
    private String chainId;

    Sign(String hexPrivateKey, String hexPublicKey, String sequence, String accountNumber, String chainId) {
        this.privateKey = new BigInteger(hexPrivateKey, 16).toByteArray();
        this.publicKey = new BigInteger(hexPublicKey, 16).toByteArray();
        this.sequence = sequence;
        this.accountNumber = accountNumber;
        this.chainId = chainId;
    }

    public JSONObject sign(JSONObject message) throws Exception {

        JSONObject signMessage = createSignMessage(message);

        String sortedJson = new Gson().toJson(jsonSort(signMessage));
        byte[] signedMessage = signWithPrivateKey(sortedJson);

        JSONObject signatureJson = createSignature(signedMessage);
        JSONObject signedTx = createSignedTx(message, signatureJson);
        JSONObject broadcastBody = createBroadcastBody(signedTx, "block");

        return broadcastBody;
    }

    private JSONObject createSignMessage(JSONObject json) {

        JSONObject json_fee = json.getJSONObject("fee");
        JSONArray amount = json_fee.getJSONArray("amount");
        Object gas = json_fee.get("gas");

        JSONObject fee = new JSONObject();
        fee.put("amount", (amount == null ? new JSONArray() : amount));
        fee.put("gas", gas);

        String memo = json.getString("memo");

        JSONObject message = new JSONObject();
        message.put("account_number", this.accountNumber);
        message.put("chain_id", this.chainId);
        message.put("fee", fee);
        message.put("memo", (memo == null ? "" : memo));
        message.put("msgs", json.get("msg"));
//        #warning("msgs : 잘 맞춰서 넣어야한다.")
        message.put("sequence", this.sequence);


        return message;
    }

    private JSONObject createSignature(byte[] signature) {
        JSONObject json = new JSONObject();

        json.put("signature", Base64.encodeBytes(signature));
        json.put("account_number", this.accountNumber);
        json.put("sequence", this.sequence);

        JSONObject sub = new JSONObject();
        sub.put("type", "tendermint/PubKeySecp256k1");
        sub.put("value", Base64.encodeBytes(this.publicKey));

        json.put("pub_key", sub);
        return json;
    }

    private byte[] signWithPrivateKey(String message) {
        byte[] messageHash = Sha256.hash(message.getBytes());

        ECKeyPair keyPair = Bip32ECKeyPair.create(this.privateKey);
        ECDSASignature signature = Sign.signMessage(messageHash, keyPair, false);
        byte[] r = signature.r.toByteArray();
        byte[] s = signature.s.toByteArray();
        int index = 0;

        int start = (r.length > 32) ? r.length - 32 : 0;
        byte[] result = new byte[r.length + s.length - start];
        for(int i=start; i<r.length; i++) {
            result[index++] = r[i];
        }
        for(int i=0; i<s.length; i++) {
            result[index++] = s[i];
        }

        return result;
    }

    private JSONObject createSignedTx(JSONObject json, JSONObject signature) {
        JSONArray array = new JSONArray();
        array.put(signature);
        json.put("signatures", array);
        return json;
    }

    private JSONObject createBroadcastBody(JSONObject json, String returnType) {
        JSONObject data = new JSONObject();
        data.put("tx", json);
        data.put("mode", returnType);
        return data;
    }

    private TreeMap jsonSort(JSONObject signMessage) throws Exception {
        TreeMap<String, Object> map = new TreeMap<String, Object>();

        for (Iterator<String> it = signMessage.keys(); it.hasNext(); ) {
            String key = it.next();
            Object obj = signMessage.get(key);

            if (obj instanceof JSONObject) {
                map.put(key, jsonSort((JSONObject) obj));
            } else if (obj instanceof JSONArray) {
                map.put(key, jsonSort((JSONArray) obj));
            } else {
                map.put(key, obj);
            }
        }

        return map;
    }

    private ArrayList jsonSort(JSONArray array) throws Exception {
        ArrayList<Object> result = new ArrayList<>();
        for(int i=0; i<array.length(); i++) {
            Object obj = array.get(i);

            if (obj instanceof JSONObject) {
                result.add(jsonSort((JSONObject) obj));
            } else if (obj instanceof JSONArray) {
                result.add(jsonSort((JSONArray) obj));
            } else {
                result.add(obj);
            }
        }

        return result;
    }
}