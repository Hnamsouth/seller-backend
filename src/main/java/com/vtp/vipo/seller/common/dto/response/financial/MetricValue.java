package com.vtp.vipo.seller.common.dto.response.financial;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MetricValue {

    // giá trị kỳ hiện tại
    private BigDecimal currentValue;

    // giá trị kỳ trước
    private BigDecimal previousValue;

    // (current - previous) => hiển thị
    private BigDecimal difference;

    // ((current - previous) / previous) * 100
    private BigDecimal growthRate;
}
