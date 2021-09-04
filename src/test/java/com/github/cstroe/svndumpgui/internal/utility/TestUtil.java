package com.github.cstroe.svndumpgui.internal.utility;

import com.google.common.io.ByteStreams;
import junit.framework.ComparisonFailure;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class TestUtil {
    public static InputStream openResource(String file) {
        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(file);
        assert s != null;
        return s;
    }

    public static void assertEqualStreams(InputStream expected, InputStream actual) throws IOException {
        byte[] expectedBytes = ByteStreams.toByteArray(expected);
        byte[] actualBytes = ByteStreams.toByteArray(actual);

        if(!Arrays.equals(expectedBytes, actualBytes)) {
            throw new ComparisonFailure("Streams differ.", new String(expectedBytes), new String(actualBytes));
        }
    }

    public static String md5sum(byte[] content) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5raw = md5.digest(content);
        return md5ConvertDigest(md5raw);
    }

    public static String md5ConvertDigest(byte[] digest) {
        return toHex(digest, 32);
    }

    // swiped from http://stackoverflow.com/questions/415953
    public static String toHex(byte[] digest, int length) {
        BigInteger bitInt = new BigInteger(1, digest);
        StringBuilder hashText = new StringBuilder(bitInt.toString(16));

        while(hashText.length() < length) {
            hashText.insert(0, "0");
        }

        return hashText.toString();
    }
}
