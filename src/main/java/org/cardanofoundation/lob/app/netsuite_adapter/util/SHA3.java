package org.cardanofoundation.lob.app.netsuite_adapter.util;

import lombok.val;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SHA3 {

    public static String digest(String data) {
        try {
            val digest = MessageDigest.getInstance("SHA3-256");

            val hashbytes = digest.digest(
                    data.getBytes(UTF_8));

            return HexFormat.of().formatHex(hashbytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
