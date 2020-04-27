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

    public String getTerraAddress() {
        byte[] sha256Hashed = Sha256.hash(publicKey32);
        byte[] ripemd160Hashed = Ripemd160.getHash(sha256Hashed);
        byte[] toWords = Bech32.toWords(ripemd160Hashed);
        String bech32Encoded = Bech32.bech32Encode(prefix.getBytes(), toWords);

        return bech32Encoded;
    }

}

