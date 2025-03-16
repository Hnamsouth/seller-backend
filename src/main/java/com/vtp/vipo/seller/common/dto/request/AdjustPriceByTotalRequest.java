package com.vtp.vipo.seller.common.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdjustPriceByTotalRequest {
    @NotNull(message = "Order ID không được để trống")
    Long orderId;

    @NotNull(message = "Ref code không được để trống")
    @NotEmpty(message = "Ref code không được để trống")
    @Size(min = 1, max = 20, message = "Ref code không được quá 20 ký tự")
    String refCode;

    @NotNull(message = "Tiền điều chỉnh không được để trống")
    @DecimalMin(value = "1", message = "Tiền điều chỉnh phải > 0")
    BigDecimal adjustedAmount;
}
