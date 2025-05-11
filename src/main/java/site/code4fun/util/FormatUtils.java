package site.code4fun.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for formatting numbers and decimals
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class FormatUtils {

    private static final int DEFAULT_SCALE = 2;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * Formats a BigDecimal number with specified scale using HALF_DOWN rounding mode
     * @param input The BigDecimal number to format
     * @param scale The number of decimal places
     * @return Formatted BigDecimal number
     * @throws IllegalArgumentException if scale is negative
     */
    public static BigDecimal formatBigDecimal(BigDecimal input, int scale) {
        if (scale < 0) {
            log.error("Invalid scale value: {}", scale);
            throw new IllegalArgumentException("Scale cannot be negative");
        }

        if (input == null) {
            log.warn("Input BigDecimal is null, returning zero");
            return ZERO;
        }

        try {
            return input.setScale(scale, RoundingMode.HALF_DOWN);
        } catch (ArithmeticException e) {
            log.error("Error formatting BigDecimal: input={}, scale={}", input, scale, e);
            return ZERO;
        }
    }

    /**
     * Formats a BigDecimal number with default scale (2 decimal places)
     * @param input The BigDecimal number to format
     * @return Formatted BigDecimal number with 2 decimal places
     */
    public static BigDecimal formatBigDecimal(BigDecimal input) {
        return formatBigDecimal(input, DEFAULT_SCALE);
    }
}
