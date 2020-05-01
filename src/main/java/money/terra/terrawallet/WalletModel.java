package money.terra.terrawallet;

import money.terra.terrawallet.library.Bech32;
import money.terra.terrawallet.library.Ripemd160;
import money.terra.terrawallet.library.Sha256;

public class WalletModel {
    private static final String prefix = "terra";

    byte[] privateKey;
    byte[] publicKey32; //compressed public key. prefix(1byte) + data(32bytes)
    byte[] publicKey64; //'un'compressed public key. prefix(1byte) + data(64bytes)
    String mnemonic;

    public String getHexPrivateKey() {
        return byteArrayToHex(privateKey);
    }

    public String getHexPublicKey32() {
        return byteArrayToHex(publicKey32);
    }

    public String getHexPublicKey64() {
        return byteArrayToHex(publicKey64);
    }

    private String byteArrayToHex(byte[] a) {

        StringBuilder sb = new StringBuilder();
        for(int i=0; i<a.length; i++) {
            byte b = a[i];
            if (i == 0 && b == 0) {
                continue;
            }

            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public String getTerraAddress() {
        byte[] sha256Hashed = Sha256.hash(publicKey32);
        byte[] ripemd160Hashed = Ripemd160.getHash(sha256Hashed);
        byte[] toWords = Bech32.toWords(ripemd160Hashed);
        String bech32Encoded = Bech32.bech32Encode(prefix.getBytes(), toWords);

        return bech32Encoded;
    }

}

