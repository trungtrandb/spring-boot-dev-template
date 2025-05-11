package site.code4fun.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@NoArgsConstructor(access = AccessLevel.NONE)
public class EncryptUtils {

    private static final char[] HEX_TABLE = "0123456789ABCDEF".toCharArray();
    private static final byte[] DECODE_HEX_ARRAY = new byte[103];

    static {
        for (int i = 0; i < 16; i++) {
            DECODE_HEX_ARRAY['0' + i] = (byte) i;
            DECODE_HEX_ARRAY['A' + i] = (byte) i;
            DECODE_HEX_ARRAY['a' + i] = (byte) i;
        }
    }

    public static String hmac(String algorithm, final String key, final String data) {
        if (StringUtils.isAnyBlank(algorithm, key, data)) {
            throw new IllegalArgumentException("Algorithm, key and data must not be null or empty");
        }

        try {
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKeySpec = new SecretKeySpec(hmacKeyBytes, algorithm);
            final Mac mac = Mac.getInstance(algorithm);
            mac.init(secretKeySpec);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = mac.doFinal(dataBytes);
            return bytesToHex(result);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error calculating HMAC: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate HMAC", e);
        }
    }

    public static String hmacSHA512(final String key, final String data) {
        return hmac("HmacSHA512", key, data);
    }

    public static String hmacSHA256(final String key, final String data) {
        if (StringUtils.isAnyBlank(key, data)) {
            throw new IllegalArgumentException("Key and data must not be null or empty");
        }

        try {
            byte[] decodedKey = decodeHex(key.getBytes(StandardCharsets.UTF_8));
            SecretKey secretKey = new SecretKeySpec(decodedKey, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(result);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            log.error("Error calculating HMAC-SHA256: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }

    public static byte[] decodeHex(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Input data must not be null or empty");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int i = 0; i < data.length; i += 2) {
                byte b1 = DECODE_HEX_ARRAY[data[i]];
                byte b2 = DECODE_HEX_ARRAY[data[i + 1]];
                out.write((b1 << 4) | b2);
            }
            return out.toByteArray();
        }
    }

    private static String bytesToHex(byte[] input) {
        StringBuilder sb = new StringBuilder(input.length * 2);
        for (byte b : input) {
            sb.append(HEX_TABLE[(b >> 4) & 0xf]);
            sb.append(HEX_TABLE[b & 0xf]);
        }
        return sb.toString();
    }
}
