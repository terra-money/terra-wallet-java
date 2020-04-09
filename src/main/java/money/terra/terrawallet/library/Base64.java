/*
 * Copyright (c) 2014 Periklis Ntanasis <pntanasis@gmail.com>
 * Distributed under the MIT License. See License.txt for more info.
 */

//MIT License

package money.terra.terrawallet.library;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

/**
 *
 * @author periklis pntanasis@gmail.com
 */
public class Base64 {

    static public String encodeBytes(byte[] words) {
        return new Base64().encode(words);
    }

    private int artificialtailing = 0;

    private static final String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
            "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3",
            "4", "5", "6", "7", "8", "9", "+", "/"};

    private static final int[] power = {1, 2, 4, 8, 16, 32, 64, 128};

    public Base64() {

    }

    private String mapAlphabet(byte[] buffer) {
        String retval = "";
        int until = buffer.length;
        if (artificialtailing == 1) {
            until--;
        }
        if (artificialtailing == 2) {
            until -= 2;
        }
        for (int i = 0; i < until; i++) {
            retval += alphabet[buffer[i]];
        }
        if (artificialtailing == 1) {
            retval += '=';
        }
        if (artificialtailing == 2) {
            retval += "==";
        }
        artificialtailing = 0;
        return retval;
    }

    public String encode(String word) throws UnsupportedEncodingException {
        return encode(word.getBytes("UTF8"));
    }

    public String encode(byte[] word) {
        BitSet bits;
        String retval;
        byte[] intarr = word;
        int bitSetSize;
        // calculate the padding
        if ((intarr.length + 1) % 3 == 0) {
            artificialtailing = 1;
        } else if ((intarr.length + 2) % 3 == 0) {
            artificialtailing = 2;
        }
        bitSetSize = (intarr.length + artificialtailing) * 8;
        bits = new BitSet(bitSetSize);
        // populate the BitSet
        int bitSetPointer = 0;
        for (int i = 0; i < intarr.length; i++) {
            for (int j = 7; j >= 0; j--) {
                bits.set(bitSetPointer++, (power[j] & intarr[i]) != 0);
            }
        }
        // create and populate the array with the alphabet indexes
        intarr = new byte[bitSetSize / 6];
        bitSetPointer = 0;
        for (int i = 0; i < intarr.length; i++) {
            byte intvalue = 0;
            for (int j = 0; j < 6; j++) {
                if (bits.get(bitSetPointer + j)) {
                    intvalue += power[5 - j];
                }
            }
            bitSetPointer += 6;
            intarr[i] = intvalue;
        }
        retval = mapAlphabet(intarr);
        return retval;
    }

    public byte[] decode(String base64) {
        BitSet bits;
        // ignore padding
        if (base64.charAt(base64.length() - 1) == '=') {
            artificialtailing = 1;
            if (base64.charAt(base64.length() - 2) == '=') {
                artificialtailing = 2;
            }
        }
        // create array of alphabet indexes (values)
        int[] alphabetIndexArray = new int[base64.length() - artificialtailing];
        for (int i = 0; i < base64.length() - artificialtailing; i++) {
            alphabetIndexArray[i] = Char2AlphabetIndex(base64.charAt(i));
        }
        // populate the bit set
        int bitSetSize = alphabetIndexArray.length * 6;
        bits = new BitSet(bitSetSize);
        int bitSetPointer = 0;
        for (int i = 0; i < alphabetIndexArray.length; i++) {
            for (int j = 5; j >= 0; j--) {
                bits.set(bitSetPointer++, (power[j] & alphabetIndexArray[i]) != 0);
            }
        }
        byte[] bytes = new byte[bitSetSize / 8];
        int bcounter = 0;
        for (int i = 0; i < bytes.length * 8; i += 8) {
            for (int j = 0; j < 8; j++) {
                if (bits.get(i + j)) {
                    bytes[bcounter] += power[7 - j];
                }
            }
            bcounter++;
        }
        artificialtailing = 0;
        return bytes;
    }

    private int Char2AlphabetIndex(char c) {
        if (c == '+') {
            return alphabet.length - 2;
        } else if (c == '/') {
            return alphabet.length - 1;
        } else if (c >= 'a') {
            return c - 'A' - 6;
        } else if (c <= '9') {
            return c - '0' + Char2AlphabetIndex('z') + 1;
        }
        return c - 'A';
    }
}