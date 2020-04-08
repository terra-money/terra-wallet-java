package money.terra.terrawallet;

import org.json.JSONObject;

public class TerraWalletSDK {
    /*
    gradle에 추가 해야함.

    implementation 'io.github.novacrypto:Hash160:2019.01.27'
    implementation 'org.web3j:crypto:4.5.17'
    implementation 'com.google.code.gson:gson'
    * */

    public static String[] getNewWallet() {
        String[] keys = KeyPair.generate(KeyPair.generateMnemonic(), 330);
        if (keys != null) {
            return keys;
        } else {
            return new String[]{"", "", "", ""};
        }
    }

    public static String[] getNewWalletFromSeed(String mnemonic, int bip) {
        String[] keys = KeyPair.generate(mnemonic, bip);
        if (keys != null) {
            return keys;
        } else {
            return new String[]{"", "", "", ""};
        }
    }

    public static JSONObject sign(JSONObject message,
                            String sequence,
                            String account_number,
                            String chain_id,
                            String hexPrivateKey,
                            String hexPublicKey) throws Exception {

        Sign sign = new Sign(hexPrivateKey, hexPublicKey, sequence, account_number, chain_id);

        return sign.sign(message);
    }
}