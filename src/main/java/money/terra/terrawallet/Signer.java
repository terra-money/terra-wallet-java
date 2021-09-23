package money.terra.terrawallet;

import com.google.gson.Gson;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import money.terra.terrawallet.library.Base64;
import money.terra.terrawallet.library.Sha256;
import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;

public class Signer {

    private byte[] privateKey;
    private byte[] publicKey;
    private String sequence;
    private String accountNumber;
    private String chainId;
    private String timeoutHeight;
    private String feeGranter;

    // options contain following new properties
    // - timeout_height: block height
    // - fee_granter: fee granter address
    Signer(String hexPrivateKey, String hexPublicKey, String sequence,
                 String accountNumber, String chainId, JSONObject options) {
        this.privateKey = new BigInteger(hexPrivateKey, 16).toByteArray();
        this.publicKey = new BigInteger(hexPublicKey, 16).toByteArray();
        this.sequence = sequence;
        this.accountNumber = accountNumber;
        this.chainId = chainId;

        this.timeoutHeight = options.optString("timeout_height", null);
        this.feeGranter = options.optString("fee_granter", null);
    }

    Signer(String hexPrivateKey, String hexPublicKey, String sequence,
                 String accountNumber, String chainId) {
        this(hexPrivateKey, hexPublicKey, sequence, accountNumber, chainId, new JSONObject());
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
        message.put("sequence", this.sequence);
        if (this.timeoutHeight != null) {
            message.put("timeout_height", this.timeoutHeight);
        }

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
        Sign.SignatureData signature =
                Sign.signMessage(messageHash, keyPair, false);
        byte[] r = signature.getR();
        byte[] s = signature.getS();
        int index = 0;

        int start = (r.length > 32) ? r.length - 32 : 0;
        byte[] result = new byte[r.length + s.length - start];
        for (int i = start; i < r.length; i++) {
            result[index++] = r[i];
        }
        for (int i = 0; i < s.length; i++) {
            result[index++] = s[i];
        }

        return result;
    }

    private JSONObject createSignedTx(JSONObject json, JSONObject signature) {
        JSONArray array = new JSONArray();
        array.put(signature);
        json.put("signatures", array);
        if (this.timeoutHeight != null) {
            json.put("timeout_height", this.timeoutHeight);
        }

        return json;
    }

    private JSONObject createBroadcastBody(JSONObject json, String returnType) {
        JSONObject data = new JSONObject();
        JSONArray sequences = new JSONArray();
        sequences.put(this.sequence);

        data.put("tx", json);
        data.put("mode", returnType);

        // optional parameters
        data.put("sequences", sequences);
        if (this.feeGranter != null) {
            data.put("fee_granter", this.feeGranter);
        }

        return data;
    }

    private TreeMap jsonSort(JSONObject signMessage) throws Exception {
        TreeMap<String, Object> map = new TreeMap<String, Object>();

        for (Iterator<String> it = signMessage.keys(); it.hasNext();) {
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
        for (int i = 0; i < array.length(); i++) {
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