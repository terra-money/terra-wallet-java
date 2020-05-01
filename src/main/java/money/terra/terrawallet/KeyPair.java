package money.terra.terrawallet;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.MnemonicUtils;

import java.math.BigInteger;
import java.security.SecureRandom;

public class KeyPair {

    static String generateMnemonic() {
        byte[] entropy = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(entropy);

        return MnemonicUtils.generateMnemonic(entropy);
    }

    static WalletModel generate(String mnemonic, int bip) {
        try {
            byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
            Bip32ECKeyPair masterKey = Bip32ECKeyPair.generateKeyPair(seed);

            int[] hdPath = {(44 | -0x80000000), (bip | -0x80000000), (0 | -0x80000000), 0, 0};
            Bip32ECKeyPair terraHD = Bip32ECKeyPair.deriveKeyPair(masterKey, hdPath);

            WalletModel model = new WalletModel();
            model.privateKey = terraHD.getPrivateKeyBytes33();
            model.publicKey32 = terraHD.getPublicKeyPoint().getEncoded(true);
            model.publicKey64 = terraHD.getPublicKeyPoint().getEncoded(false); //'un'compressed public key.
            model.mnemonic = mnemonic;

            return model;
        }catch(Exception e) {
            return null;
        }
    }
}
