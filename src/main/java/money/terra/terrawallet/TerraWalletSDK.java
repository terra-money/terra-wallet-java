package money.terra.terrawallet;

import org.json.JSONObject;

public class TerraWalletSDK {

    public static String[] getNewWallet() {
        return getNewWalletFromSeed(KeyPair.generateMnemonic(), 330);
    }

    public static String[] getNewWalletFromSeed(String mnemonic) {
        return getNewWalletFromSeed(mnemonic, 330);
    }

    public static String[] getNewWalletFromSeed(String mnemonic, int bip) {
        WalletModel wallet = KeyPair.generate(mnemonic, bip);
        try {
            String hexPrivateKey = KeyPair.byteArrayToHex(wallet.privateKey);
            String hexPublicKey32 = KeyPair.byteArrayToHex(wallet.publicKey32);
            String hexPublicKey64 = KeyPair.byteArrayToHex(wallet.publicKey64); //'un'compressed public key.
            String terraAddress = wallet.getTerraAddress();
            return new String[]{hexPrivateKey, hexPublicKey32, hexPublicKey64, terraAddress, mnemonic};
        } catch (Exception e) {
            return new String[]{"", "", "", "", ""};
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