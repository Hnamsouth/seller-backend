package com.vtp.vipo.seller.common.dto.request.product;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.common.utils.Translator;
import com.vtp.vipo.seller.config.validation.annotation.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.vtp.vipo.seller.common.dto.request.product.SellingProductInfo.EACH_SKU_OF_PRODUCT;
import static com.vtp.vipo.seller.common.dto.request.product.SellingProductInfo.UNIT_PRICE_BY_SKU;

/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
/*Quản lý mã hàng hóa*/
public class ManageProductCodeInfo {
    @NotNull
    @IsTrim
    String productImage;         // Ảnh sản phẩm
    @NotNull
    @IsTrim
    ArrayList<String> attribute;            // Thuộc tính

    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
    @IsInteger
    BigDecimal unitPrice;        // Đơn Giá
    @NotNull
    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
    Long stock;                   // Tồn kho
    @NotNull
    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
    Integer minPurchase;             // Mua tối thiểu
    @NotNull
    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
    Integer weight;                  // Cân nặng (gram)
    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
    @MaxScale(value = 3, errorResponse = ErrorCodeResponse.INVALID_SCALE_PERCENTAGE)
    Double length;                  // Chiều dài (cm)
    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
    @MaxScale(value = 3, errorResponse = ErrorCodeResponse.INVALID_SCALE_PERCENTAGE)
    Double width;                   // Chiều rộng (cm)
    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
    @MaxScale(value = 3, errorResponse = ErrorCodeResponse.INVALID_SCALE_PERCENTAGE)
    Double height;                  // Chiều cao (cm)
    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
    @IsInteger
    BigDecimal shippingFee;      // Phí vận chuyển
    boolean activeStatus = true; // Trạng thái hoạt động
    String productSkuCodeCustomer;

    public String getStrAttribute() {
        return String.join("-", this.attribute);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManageProductCodeInfo that = (ManageProductCodeInfo) o;
        return Objects.equals(productImage, that.productImage) &&
                Objects.equals(attribute, that.attribute) &&
                Objects.equals(unitPrice, that.unitPrice) &&
                Objects.equals(stock, that.stock) &&
                Objects.equals(minPurchase, that.minPurchase) &&
                Objects.equals(weight, that.weight) &&
                Objects.equals(length, that.length) &&
                Objects.equals(width, that.width) &&
                Objects.equals(height, that.height) &&
                Objects.equals(shippingFee, that.shippingFee) &&
                Objects.equals(activeStatus, that.activeStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productImage, attribute, unitPrice, stock, minPurchase, weight, length, width, height, shippingFee, activeStatus);
    }

    public String validate(Integer priceType,Integer minPurchaseType, boolean needString) {
        if (UNIT_PRICE_BY_SKU.equals(priceType)) {
            if (DataUtils.isNullOrEmpty(unitPrice)) {
                if (needString) {
                    return Translator.toLocale("invalid.unitPrice.sku.required");
                } else {
                    throw new VipoBusinessException(ErrorCodeResponse.INVALID_REQUIRED_FIELD);
                }
            }
            if ( EACH_SKU_OF_PRODUCT.equals(minPurchaseType)
                    &&DataUtils.isNullOrEmpty(minPurchase)) {
                if (needString) {
                    return Translator.toLocale("invalid.minPurchase.sku.required");
                } else {
                    throw new VipoBusinessException(ErrorCodeResponse.INVALID_REQUIRED_FIELD);
                }
            }
        }
        return null;
    }

    /* Phase 5.5: Product Approval Fix:
    * store seller_classify.id */
    private List<Long> sellerClassifyIds;

    private Long id;

    private String tempId;

    private List<String> sellerClassifyTempIds;

}
