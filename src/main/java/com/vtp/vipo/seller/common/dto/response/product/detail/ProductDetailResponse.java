package com.vtp.vipo.seller.common.dto.response.product.detail;

import com.vtp.vipo.seller.common.dto.request.product.*;
import com.vtp.vipo.seller.common.dto.response.ProductCertificateResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

/**
 * Author: hieuhm12
 * Date: 9/16/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetailResponse {
    /*id bảng product*/
    Long id;
    /*Thông tin cơ bản*/
    BaseProductInfo baseProductInfo;
    /*Thông tin bán hàng*/
    SellingProductInfo sellingProductInfo;
    /*Thông số sản phẩm*/
    Set<ProductSpecInfo> productSpecInfo;
    /*Thông tin thuộc tính-Thông tin phân loại*/
    Set<ProductAttributesDetailResponse> productAttributesInfo;
    /*Quản lý mã hàng hóa*/
    Set<ManageProductCodeDetailResponse> manageProductCodeInfo;
    Set<StepPriceInfo> stepPriceInfo;
    Integer status;

    /*Chứng chỉ sản phẩm*/
    Set<ProductCertificateResponse> certificatesInfo;

    /* Phase 5.5: Product Approval Fix: Flag to check if the product is allowed to Add or remove seller_attribute */
    boolean allowToAddOrRemoveAttribute = true;

    String blockAddingOrRemovingAttributeMsg;



}
