package money.terra.terrawallet;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.math.ec.ECPoint;

public class KeyPair {

    static String generateMnemonic() {
        byte[] entropy = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(entropy);

        return MnemonicUtils.generateMnemonic(entropy);
    }

    static WalletModel generateFromMnemonic(String mnemonic, int bip) {
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

    static WalletModel generateFromPrivkey(String privkey) {
        ECKeyPair kp = ECKeyPair.create(new BigInteger(privkey, 16));
        WalletModel model = new WalletModel();

        ECPoint publicKeyPt = Sign.publicPointFromPrivate(kp.getPrivateKey());
        
        byte[] bytes33 = new byte[33];
        byte[] priv = bigIntegerToBytes32(kp.getPrivateKey());
        System.arraycopy(priv, 0, bytes33, 33 - priv.length, priv.length);

        model.privateKey = bytes33;
        model.publicKey32 = publicKeyPt.getEncoded(true);
        model.publicKey64 = publicKeyPt.getEncoded(false);
        model.mnemonic = "N/A";

        return model;
    }

    // Code from web3j project
    private static byte[] bigIntegerToBytes32(BigInteger b) {
        final int numBytes = 32;

        byte[] src = b.toByteArray();
        byte[] dest = new byte[numBytes];
        boolean isFirstByteOnlyForSign = src[0] == 0;
        int length = isFirstByteOnlyForSign ? src.length - 1 : src.length;
        int srcPos = isFirstByteOnlyForSign ? 1 : 0;
        int destPos = numBytes - length;
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }
}
