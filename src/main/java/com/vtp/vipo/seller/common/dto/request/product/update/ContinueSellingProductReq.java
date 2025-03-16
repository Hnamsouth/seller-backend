package com.vtp.vipo.seller.common.dto.request.product.update;

import com.vtp.vipo.seller.common.dto.request.product.ManageProductCodeInfo;
import com.vtp.vipo.seller.common.dto.request.product.ProductAttributesInfo;
import com.vtp.vipo.seller.common.dto.request.product.ProductSpecInfo;
import com.vtp.vipo.seller.common.dto.request.product.StepPriceInfo;
import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.config.validation.annotation.MaxListSize;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
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
public class ContinueSellingProductReq {
    @NotNull
    Long id;
    @NotNull
    /*Thông ton kho product_seller_sku.id --- So luong san pham*/
    LinkedHashMap<Long, Long> mapStock;

    public void validate() {
        for (Map.Entry<Long, Long> entry : mapStock.entrySet()) {
            Long key = entry.getKey();
            Long value = entry.getKey();
            if (DataUtils.isNullOrEmpty(key) || DataUtils.isNullOrEmpty(value)) {
                throw new VipoBusinessException(ErrorCodeResponse.REQUIRED_FIELD,"mapStock");
            }
            if (value < 0L) {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_REQUIRED_FIELD);
            }
        }
    }
}
