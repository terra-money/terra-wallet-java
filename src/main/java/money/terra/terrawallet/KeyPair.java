package money.terra.terrawallet;

import money.terra.terrawallet.Bech32.Bech32;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.MnemonicUtils;

import java.security.SecureRandom;

import io.github.novacrypto.hashing.Hash160;

public class KeyPair {
    private static final String prefix = "terra";

    static String getTerraAddress(byte[] publicKey) {
        return Bech32.encode(prefix, Bech32.toWords(Hash160.hash160(publicKey)));
    }

    static String generateMnemonic() {
        byte[] entropy = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(entropy);

        return MnemonicUtils.generateMnemonic(entropy);
    }

    static String[] generate(String mnemonic, int bip) {
        try {
            byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
            Bip32ECKeyPair masterKey = Bip32ECKeyPair.generateKeyPair(seed);

            int[] hdPathLuna = {(44 | -0x80000000), (bip | -0x80000000), (0 | -0x80000000), 0, 0};
            Bip32ECKeyPair terraHD = Bip32ECKeyPair.deriveKeyPair(masterKey, hdPathLuna);

            byte[] privateKeyData = terraHD.getPrivateKeyBytes33();
            byte[] publicKeyData = terraHD.getPublicKeyPoint().getEncoded(true);

            String[] params = new String[4];
            params[0] = byteArrayToHex(privateKeyData);
            params[1] = byteArrayToHex(publicKeyData);
            params[2] = getTerraAddress(publicKeyData);
            params[3] = mnemonic;

            return params;
        }catch(Exception e) {
            return null;
        }
    }

    private static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(final byte b: a) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString().replaceFirst("00","");
    }
}
