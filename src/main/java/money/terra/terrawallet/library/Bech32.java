package money.terra.terrawallet.library;

//https://github.com/WebOfTrustInfo/txref-conversion-java/blob/master/src/main/java/info/weboftrust/txrefconversion/Bech32.java
//MIT license

import java.nio.ByteBuffer;

public class Bech32 {

    public static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";

    private Bech32() { }

    public static String bech32Encode(byte[] hrp, byte[] data) {

        byte[] chk = createChecksum(hrp, data);
        byte[] combined = new byte[chk.length + data.length];

        System.arraycopy(data, 0, combined, 0, data.length);
        System.arraycopy(chk, 0, combined, data.length, chk.length);

        byte[] xlat = new byte[combined.length];
        for (int i = 0; i < combined.length; i++) {
            xlat[i] = (byte)CHARSET.charAt(combined[i]);
        }

        byte[] ret = new byte[hrp.length + xlat.length + 1];
        System.arraycopy(hrp, 0, ret, 0, hrp.length);
        System.arraycopy(new byte[] { 0x31 }, 0, ret, hrp.length, 1);
        System.arraycopy(xlat, 0, ret, hrp.length + 1, xlat.length);

        return new String(ret);
    }

    private static int polymod(byte[] values)  {

        final int[] GENERATORS = { 0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3 };

        int chk = 1;

        for (byte b : values) {
            byte top = (byte)(chk >> 0x19);
            chk = b ^ ((chk & 0x1ffffff) << 5);
            for (int i = 0; i < 5; i++) {
                chk ^= ((top >> i) & 1) == 1 ? GENERATORS[i] : 0;
            }
        }

        return chk;
    }

    private static byte[] hrpExpand(byte[] hrp) {

        byte[] buf1 = new byte[hrp.length];
        byte[] buf2 = new byte[hrp.length];
        byte[] mid = new byte[1];

        for (int i = 0; i < hrp.length; i++) {
            buf1[i] = (byte)(hrp[i] >> 5);
        }
        mid[0] = 0x00;
        for (int i = 0; i < hrp.length; i++) {
            buf2[i] = (byte)(hrp[i] & 0x1f);
        }

        byte[] ret = new byte[(hrp.length * 2) + 1];
        System.arraycopy(buf1, 0, ret, 0, buf1.length);
        System.arraycopy(mid, 0, ret, buf1.length, mid.length);
        System.arraycopy(buf2, 0, ret, buf1.length + mid.length, buf2.length);

        return ret;
    }

    private static byte[] createChecksum(byte[] hrp, byte[] data)  {

        byte[] zeroes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        byte[] expanded = hrpExpand(hrp);
        byte[] values = new byte[zeroes.length + expanded.length + data.length];

        System.arraycopy(expanded, 0, values, 0, expanded.length);
        System.arraycopy(data, 0, values, expanded.length, data.length);
        System.arraycopy(zeroes, 0, values, expanded.length + data.length, zeroes.length);

        int polymod = polymod(values) ^ 1;
        byte[] ret = new byte[6];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte)((polymod >> 5 * (5 - i)) & 0x1f);
        }

        return ret;
    }

    public static byte[] toWords(byte[] data) {
        final int inBits = 8;
        final int outBits = 5;
        final boolean pad = true;

        int value = 0;
        int bits = 0;
        int maxV = (1 << outBits) - 1;

        ByteBuffer result = ByteBuffer.allocate(maxV + 1);
        for (byte datum : data) {
            value = (value << inBits) | (datum & 0xFF);
            bits += inBits;

            while (bits >= outBits) {
                bits -= outBits;
                result.put((byte) ((value >> bits) & maxV));
            }
        }

        if (pad) {
            if (bits > 0) {
                result.put((byte) ((value << (outBits - bits)) & maxV));
            }
        } else {
            if (bits >= inBits) throw new IllegalStateException("Excess padding");
            if (((value << (outBits - bits)) & maxV) > 0)
                throw new IllegalStateException("Non-zero padding");
        }

        return result.array();
    }
}