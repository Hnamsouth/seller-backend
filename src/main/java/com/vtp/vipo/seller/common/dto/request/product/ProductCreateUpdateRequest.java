package com.vtp.vipo.seller.common.dto.request.product;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.config.validation.annotation.MaxListSize;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;
/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreateUpdateRequest {
    /*id bảng product*/
    Long id;
    @NotNull
    @Valid
    /*Thông tin cơ bản*/
    BaseProductInfo baseProductInfo;
    @NotNull
    @Valid
    /*Thông tin bán hàng*/
    SellingProductInfo sellingProductInfo;
    @NotNull
    @MaxListSize(value = 20,errorResponse = ErrorCodeResponse.INVALID_PROD_SPEC_INFO_SIZE)
    @Valid
    /*Thông số sản phẩm*/
    Set<ProductSpecInfo> productSpecInfo;
    @NotNull
    @MaxListSize(value = 3,errorResponse = ErrorCodeResponse.INVALID_PROD_ATTRIBUTE_INFO_SIZE)
    @Valid
    /*Thông tin thuộc tính-Thông tin phân loại*/
    Set<ProductAttributesInfo> productAttributesInfo;
    @NotNull
    @Valid
    /*Quản lý mã hàng hóa*/
    Set<ManageProductCodeInfo> manageProductCodeInfo;
    @Valid
    Set<StepPriceInfo> stepPriceInfo;
    Boolean isDraft = Boolean.FALSE;

    /*Chứng chỉ sản phẩm*/
    Set<CertificateRequest> certificatesInfo;
}
