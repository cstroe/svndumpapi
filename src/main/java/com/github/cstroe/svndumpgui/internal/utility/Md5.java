package com.github.cstroe.svndumpgui.internal.utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 implements HashGenerator {

    @Override
    public String hash(byte[] content) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5raw = md5.digest(content);
            return toHex(md5raw, 32);
        } catch(NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}
