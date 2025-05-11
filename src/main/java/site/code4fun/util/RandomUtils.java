package site.code4fun.util;

import com.yubico.webauthn.data.ByteArray;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

/**
 * Utility class for generating random values.
 * This class uses {@link SecureRandom} for cryptographically strong random number generation.
 * All methods in this class are thread-safe.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
@SuppressWarnings("unused")
public final class RandomUtils {
    private static final SecureRandom random = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Generates a random string of specified length using alphanumeric characters.
     *
     * @param count the length of the random string to generate
     * @return a random string of specified length
     * @throws IllegalArgumentException if count is negative
     */
    public static String random(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must be non-negative");
        }
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    /**
     * Generates a random integer within the specified range.
     *
     * @param from the lower bound (inclusive)
     * @param to the upper bound (exclusive)
     * @return a random integer between from (inclusive) and to (exclusive)
     * @throws IllegalArgumentException if from is greater than or equal to to
     */
    public static int getRandomNumberInRange(int from, int to) {
        if (from >= to) {
            throw new IllegalArgumentException("from must be less than to");
        }
        return random.nextInt(to - from) + from;
    }

    /**
     * Generates a random string of specified length using only alphabetic characters.
     *
     * @param length the length of the random string to generate
     * @return a random string containing only alphabetic characters
     * @throws IllegalArgumentException if length is negative
     */
    public static String randomAlphabet(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length must be non-negative");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    /**
     * Generates a random byte array of specified length.
     *
     * @param length the length of the random byte array to generate
     * @return a ByteArray containing random bytes
     * @throws IllegalArgumentException if length is negative
     */
    public static ByteArray generateRandom(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length must be non-negative");
        }
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return new ByteArray(bytes);
    }
}
