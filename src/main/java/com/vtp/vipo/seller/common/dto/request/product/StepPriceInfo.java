package com.vtp.vipo.seller.common.dto.request.product;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.GreaterThan;
import com.vtp.vipo.seller.config.validation.annotation.IsInteger;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
/*Cấu hình thang giá*/
public class StepPriceInfo {
    @NotNull
    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
    Integer priceStep = 1;            // Thang giá
    Integer fromQuantity;            // Từ (sản phẩm)
    Integer toQuantity;              // Đến (sản phẩm)
    @NotNull
    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
    @IsInteger
    BigDecimal unitPrice;        // Đơn giá

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepPriceInfo that = (StepPriceInfo) o;
        return Objects.equals(priceStep, that.priceStep);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priceStep);
    }
}
