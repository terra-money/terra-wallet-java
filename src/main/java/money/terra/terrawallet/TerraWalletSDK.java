package money.terra.terrawallet;

import money.terra.terrawallet.library.Bech32;
import org.json.JSONObject;

import java.math.BigInteger;

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
            String hexPrivateKey = wallet.getHexPrivateKey();
            String hexPublicKey32 = wallet.getHexPublicKey32();
            String hexPublicKey64 = wallet.getHexPublicKey64(); //'un'compressed public key.
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

        return new Signer(hexPrivateKey, hexPublicKey, sequence, account_number, chain_id).sign(message);
    }

    public static boolean isValidAddress(String address) {
        try {
            Bech32.HrpAndData result = Bech32.bech32Decode(address);
            String hrp = new String(result.hrp);
            if(!hrp.equals("terra")) {
                return false;
            }

            String recovered = Bech32.bech32Encode(result.hrp, result.data);
            return recovered.equals(address);
        }catch(Exception e) {
            return false;
        }
    }
}