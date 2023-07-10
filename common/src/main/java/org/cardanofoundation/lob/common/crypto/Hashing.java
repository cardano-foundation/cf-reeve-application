package org.cardanofoundation.lob.common.crypto;

import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.util.encoders.Hex;

import java.util.List;

public class Hashing {
    public static final int BLAKE2B_224_DIGEST_BYTES_LENGTH = 28;
    public static final int BLAKE2B_256_DIGEST_BYTES_LENGTH = 32;

    public static byte[] blake2bDigest(final List<byte[]> inputs, final int digestBytes) {
        final Blake2bDigest b2b = new Blake2bDigest(digestBytes * 8);
        for (final byte[] input : inputs) {
            b2b.update(input, 0, input.length);
        }
        final byte[] digestRaw = new byte[b2b.getDigestSize()];
        b2b.doFinal(digestRaw, 0);
        return digestRaw;
    }

    public static byte[] blake2b224Digest(final byte[] input) {
        return blake2bDigest(List.of(input), BLAKE2B_224_DIGEST_BYTES_LENGTH);
    }

    public static byte[] blake2b224Digest(final List<byte[]> inputs) {
        return blake2bDigest(inputs, BLAKE2B_224_DIGEST_BYTES_LENGTH);
    }

    public static byte[] blake2b256Digest(final byte[] input) {
        return blake2bDigest(List.of(input), BLAKE2B_256_DIGEST_BYTES_LENGTH);
    }

    public static byte[] blake2b256Digest(final List<byte[]> inputs) {
        return blake2bDigest(inputs, BLAKE2B_256_DIGEST_BYTES_LENGTH);
    }

    public static String blake2b224Hex(final byte[] input) {
        return Hex.toHexString(blake2b224Digest(input));
    }

    public static String blake2b224Hex(final List<byte[]> inputs) {
        return Hex.toHexString(blake2b224Digest(inputs));
    }

    public static String blake2b256Hex(final byte[] input) {
        return Hex.toHexString(blake2b256Digest(input));
    }

    public static String blake2b256Hex(final List<byte[]> inputs) {
        return Hex.toHexString(blake2b256Digest(inputs));
    }
}