package com.vtp.vipo.seller.common.dto.request.product.update;

import com.vtp.vipo.seller.common.dto.request.product.ProductSpecInfo;
import com.vtp.vipo.seller.common.dto.request.product.SellingProductInfo;
import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.config.validation.annotation.MaxListSize;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.Valid;
import java.util.Set;

/**
 * Author: hieuhm12
 * Date: 9/18/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateSpecInfoProductReq {
    @NotNull
    Long id;
    @NotNull
    @MaxListSize(value = 20,errorResponse = ErrorCodeResponse.INVALID_PROD_SPEC_INFO_SIZE)
    @Valid
    /*Thông số sản phẩm*/
    Set<ProductSpecInfo> productSpecInfo;
}
