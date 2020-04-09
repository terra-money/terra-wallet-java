/*
 * Copyright (c) 2014 Periklis Ntanasis <pntanasis@gmail.com>
 * Distributed under the MIT License. See License.txt for more info.
 */

//MIT License

package money.terra.terrawallet.library;

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
}