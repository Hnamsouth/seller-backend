package com.vtp.vipo.seller.common.dto.request.product;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;

import jakarta.persistence.OneToMany;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
/*Thông tin bán hàng*/
public class SellingProductInfo {
    /*0: Đồng giá SKU không thang giá */
    public static final Integer FIXED_PRICE_SKU_NO_PRICE_STEP = 0;
    /*1: Đồng giá SKU có thang giá*/
    public static final Integer FIXED_PRICE_SKU_WITH_PRICE_STEP = 1;
    /*2: Đơn giá theo SKU*/
    public static final Integer UNIT_PRICE_BY_SKU = 2;
    /*0: Cộng dồn cả sản phẩm */
    public static final Integer AGGREGATE_ALL_PRODUCTS = 0;
    /*1: Từng SKU của sản phẩm */
    public static final Integer EACH_SKU_OF_PRODUCT = 1;
    //  @NotNull
    @InRange(min = "0", max = "100", errorResponse = ErrorCodeResponse.INVALID_RANGE_PERCENTAGE)
    @MaxScale(value = 2, errorResponse = ErrorCodeResponse.INVALID_SCALE_PERCENTAGE)
    BigDecimal platformDiscountRate;    // Nhập % chiết khấu cho sàn
    /*0: Đồng giá SKU không thang giá
      1: Đồng giá SKU có thang giá
      2: Đơn giá theo SKU*/
    @NotNull
    @OneOf(collection = {"0", "1", "2"}, errorResponse = ErrorCodeResponse.INVALID_VALUE_SET)
    Integer priceType;                   // Chọn loại giá áp dụng
    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
    @MaxScale(value = 2, errorResponse = ErrorCodeResponse.INVALID_SCALE_PERCENTAGE)
    BigDecimal productPrice;            // Nhập giá sản phẩm
    @NotNull
    @OneOf(collection = {"0", "1"}, errorResponse = ErrorCodeResponse.INVALID_VALUE_SET)
    /*0: Cộng dồn cả sản phẩm
      1: Từng SKU của sản phẩm */
            Integer minPurchaseType;             // Chọn loại mua tối thiểu áp dụng
    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
    Integer minPurchaseQuantity;            // Nhập số lượng mua tối thiểu cả sản phẩm

    public String validate(Long totalStock, boolean needString) {
        //Giá sản phần phải có nếu Chọn loại giá áp dụng = Đồng giá SKU không thang giá
        if (FIXED_PRICE_SKU_NO_PRICE_STEP.equals(priceType)
                && ObjectUtils.isEmpty(productPrice)) {
            if (needString) {
                return ErrorCodeResponse.REQUIRED_FIELD.getMessageI18N();
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.REQUIRED_FIELD, "productPrice");
            }
        }
        //loại mua tối thiểu áp dụng phải có nếu loại mua tối thiểu= Cộng dồn cả sản phẩm
        if (AGGREGATE_ALL_PRODUCTS.equals(minPurchaseType)
                && ObjectUtils.isEmpty(minPurchaseQuantity)) {
            if (needString) {
                return ErrorCodeResponse.REQUIRED_FIELD.getMessageI18N();
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.REQUIRED_FIELD, "minPurchaseQuantity");
            }
        }
        //Mua tối thieu khong vuot qua ton kho
        if (ObjectUtils.isNotEmpty(minPurchaseQuantity)
                && Long.valueOf(minPurchaseQuantity) > totalStock) {
            if (needString) {
                return ErrorCodeResponse.INVALID_EXCEED_STOCK.getMessageI18N();
            } else {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_EXCEED_STOCK);
            }
        }
        return null;
    }
}
