package com.vtp.vipo.seller.services.financial;

import com.vtp.vipo.seller.common.dto.response.financial.MetricValue;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FinancialUtils {
    /**
     * Returns the provided BigDecimal value if it is not null; otherwise, returns BigDecimal.ZERO.
     *
     * @param val the BigDecimal value to check
     * @return the original value if it is not null, or BigDecimal.ZERO if it is null
     */
    public static BigDecimal safe(BigDecimal val) {
        return ObjectUtils.isNotEmpty(val) ? val : BigDecimal.ZERO;
    }

    /**
     * Converts a long value to a BigDecimal.
     *
     * @param val the long value to convert
     * @return a BigDecimal representation of the given long value
     */
    public static BigDecimal toBigDecimal(long val) {
        return BigDecimal.valueOf(val);
    }

    /**
     * Rounds the given BigDecimal to two decimal places using HALF_UP rounding mode.
     * <p>
     * If the provided value is null, it returns BigDecimal.ZERO.
     * </p>
     *
     * @param value the BigDecimal value to round
     * @return the rounded value with two decimal places, or BigDecimal.ZERO if the value is null
     */
    public static BigDecimal roundToTwoDecimalPlaces(BigDecimal value) {
        if (ObjectUtils.isEmpty(value)) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Returns a safe MetricValue.
     * <p>
     * If the given metric is null, this method returns a default MetricValue where all fields
     * (currentValue, previousValue, difference, and growthRate) are set to BigDecimal.ZERO.
     * Otherwise, it returns the provided metric.
     * </p>
     *
     * @param metric the MetricValue to check
     * @return the original MetricValue if it is not null, or a default MetricValue with zeros if it is null
     */
    public static MetricValue safeMetric(MetricValue metric) {
        if (ObjectUtils.isEmpty(metric)) {
            return MetricValue.builder()
                    .currentValue(BigDecimal.ZERO)
                    .previousValue(BigDecimal.ZERO)
                    .difference(BigDecimal.ZERO)
                    .growthRate(BigDecimal.ZERO)
                    .build();
        }
        return metric;
    }
}
