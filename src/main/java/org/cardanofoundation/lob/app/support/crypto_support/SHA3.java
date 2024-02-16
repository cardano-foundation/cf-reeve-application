package org.cardanofoundation.lob.app.support.crypto_support;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SHA3 {

    public static String digestAsBase64(String data) {
        try {
            var digest = MessageDigest.getInstance("SHA3-256");

            var hashbytes = digest.digest(
                    data.getBytes(UTF_8));

            return Base64.getEncoder().encodeToString(hashbytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
