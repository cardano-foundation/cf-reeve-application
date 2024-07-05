package org.cardanofoundation.lob.app.support.crypto;

import com.bloxbean.cardano.client.util.HexUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SHA3 {

    public static String digestAsHex(int data) {
        return digestAsHex(String.valueOf(data));
    }

    public static String digestAsHex(String data) {
        try {
            var digest = MessageDigest.getInstance("SHA3-256");

            var hashbytes = digest.digest(
                    data.getBytes(UTF_8));

            return HexUtil.encodeHexString(hashbytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
