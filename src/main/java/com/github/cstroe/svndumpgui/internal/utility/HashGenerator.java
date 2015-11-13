package com.github.cstroe.svndumpgui.internal.utility;

import java.math.BigInteger;

public interface HashGenerator {
    String hash(byte[] content);

    // swiped from http://stackoverflow.com/questions/415953
    default String toHex(byte[] digest, int length) {
        BigInteger bitInt = new BigInteger(1, digest);
        String hashText = bitInt.toString(16);

        while(hashText.length() < length) {
            hashText = "0" + hashText;
        }

        return hashText;
    }
}
