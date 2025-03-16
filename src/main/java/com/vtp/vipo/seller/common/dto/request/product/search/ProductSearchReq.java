package com.vtp.vipo.seller.common.dto.request.product.search;

import com.vtp.vipo.seller.common.enumseller.ProductStatus;
import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.config.validation.annotation.MinLength;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Author: hieuhm12
 * Date: 9/17/2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSearchReq {
    @MinLength(value = 3,errorResponse = ErrorCodeResponse.INVALID_REQUIRED_FIELD)
    String key;
    Integer categoryId;
    Integer status;
    @NotNull
    Instant fromDate;
    @NotNull
    Instant toDate;

    public String getKey() {
        return DataUtils.makeLikeParam(key);
    }

    public ProductStatus getStatus() {
        if(DataUtils.isNullOrEmpty(status)) return null;
        return ProductStatus.fromValue(status);
    }

    public Instant getFromDate() {
        return DateUtils.truncateToMidnight(fromDate);
    }

    public Instant getToDate() {
        return DateUtils.truncateToMidnight(toDate);
    }
}
