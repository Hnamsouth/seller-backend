package com.vtp.vipo.seller.common.dto.request.product.update;

import com.vtp.vipo.seller.common.dto.request.product.*;
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
public class UpdateAttributeProductReq {
    @NotNull
    Long id;
    @NotNull
    @MaxListSize(value = 3,errorResponse = ErrorCodeResponse.INVALID_PROD_ATTRIBUTE_INFO_SIZE)
    @Valid
    /*Thông tin thuộc tính-Thông tin phân loại*/
    Set<ProductAttributesInfo> productAttributesInfo;
    @NotNull
    @Valid
    /*Quản lý mã hàng hóa*/
    Set<ManageProductCodeInfo> manageProductCodeInfo;
    @NotNull
    @Valid
    Set<StepPriceInfo> stepPriceInfo;
}
