package com.vtp.vipo.seller.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Objects;

@Slf4j
public final class NumUtils {

    private NumUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static BigDecimal sumBigDecimals(BigDecimal... values) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            if (ObjectUtils.isNotEmpty(value)) {
                sum = sum.add(value);
            }
        }
        return sum;
    }

    public static BigDecimal sumBigDecimals(Collection<BigDecimal> values) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            if (ObjectUtils.isNotEmpty(value)) {
                sum = sum.add(value);
            }
        }
        return sum;
    }

    /**
     * Subtracts multiple BigDecimal values starting from the first element.
     * - If the first element is null, returns BigDecimal.ZERO.
     * - If subsequent elements are null, treats them as BigDecimal.ZERO.
     *
     * @param values Varargs of BigDecimal values to subtract.
     * @return The result of the subtraction.
     */
    public static BigDecimal minusBigDecimals(BigDecimal... values) {
        if (values == null || values.length == 0) {
            return BigDecimal.ZERO;
        }

        // Check the first element
        BigDecimal result = (values[0] != null) ? values[0] : BigDecimal.ZERO;

        // Iterate over the rest of the elements, starting from index 1
        for (int i = 1; i < values.length; i++) {
            BigDecimal value = values[i];
            // Treat null as BigDecimal.ZERO
            BigDecimal toSubtract = (value != null) ? value : BigDecimal.ZERO;
            result = result.subtract(toSubtract);
        }

        return result;
    }

    public static BigDecimal multiplyBigDecimals(BigDecimal... values) {
        if (Arrays.stream(values).anyMatch(ObjectUtils::isEmpty)) {
            return null;
        }
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            if (ObjectUtils.isNotEmpty(value)) {
                result = result.subtract(value);
            }
        }
        return result;
    }

    public static Long sumLongs(Long... values) {
        long sum = 0L;
        for (Long value : values) {
            if (ObjectUtils.isNotEmpty(value)) {
                sum = sum + (value);
            }
        }
        return sum;
    }

    public static List<Long> convertStringToLongList(String input) {
        if (input == null || input.isBlank()) {
            return new ArrayList<>(); // Return an empty immutable list
        }

        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(NumUtils::safeParseLong)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static java.util.Optional<Long> safeParseLong(String s) {
        try {
            return java.util.Optional.of(Long.parseLong(s));
        } catch (NumberFormatException e) {
            // Optionally log the error
            log.info("Skipping invalid entry: '" + s + "'");
            return java.util.Optional.empty();
        }
    }

    /**
     * Formats a BigDecimal value to have no decimal places (rounded half-up)
     * and groups digits in groups of three separated by dots.
     *
     * For example, 3000000000 becomes "3.000.000.000".
     *
     * @param value the BigDecimal to format
     * @return a formatted String representation of the BigDecimal
     */
    public static String formatBigDecimalToVNDFormat(BigDecimal value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }

        // Round the value to no decimal places using HALF_UP rounding mode
        BigDecimal roundedValue = value.setScale(0, RoundingMode.HALF_UP);

        // Create a DecimalFormatSymbols instance with dot as grouping separator
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');

        // Define a DecimalFormat with the desired pattern
        // The pattern "#,##0" means group digits (e.g., 3.000.000.000) and no decimal part.
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        formatter.setGroupingUsed(true);
        formatter.setRoundingMode(RoundingMode.HALF_UP);

        return formatter.format(roundedValue);
    }

}
