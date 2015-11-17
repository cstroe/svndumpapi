package com.github.cstroe.svndumpgui.internal.utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1 implements HashGenerator {
    @Override
    public String hash(byte[] content) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            byte[] sha1raw = sha1.digest(content);
            return toHex(sha1raw, 40);
        } catch(NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}
