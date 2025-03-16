package com.vtp.vipo.seller.common.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdjustPriceBySkuRequest {
    @NotNull(message = "Order ID không được để trống")
    Long orderId;

    @NotNull(message = "Ref code không được để trống")
    @NotEmpty(message = "Ref code không được để trống")
    @Size(min = 1, max = 20, message = "Ref code không được quá 20 ký tự")
    String refCode;

    @NotNull(message = "Danh sách sku không được để trống")
    @Size(min = 1, message = "Danh sách sku không được để trống")
    List<@Valid SkuItem> skus;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SkuItem {
        @NotNull(message = "Sku id không được để trống")
        @NotEmpty(message = "Sku id không được để trống")
        String skuId;

        @NotNull(message = "Tiền điều chỉnh không được để trống")
        @DecimalMin(value = "1", message = "Tiền điều chỉnh phải > 0")
        BigDecimal adjustedAmount;
    }
}
