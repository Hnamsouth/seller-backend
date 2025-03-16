package com.vtp.vipo.seller.common.dto.response.product.create;

import com.vtp.vipo.seller.common.dto.request.product.*;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.common.utils.StackTraceUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.vtp.vipo.seller.common.dto.request.product.SellingProductInfo.FIXED_PRICE_SKU_WITH_PRICE_STEP;

/**
 * Author: hieuhm12
 * Date: 9/20/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuCreateByFileExcel extends ManageProductCodeInfo {
    String productCodeCustomer;//Mã sản phẩm tu trang 1
    String imageSku;//Anh sku
    String classifyInfo;//Thông tin phân loại SKU
    String unitPriceStr;//Đơn giá
    String stockStr;//Tồn kho
    String minPurchaseStr;//Mua tối thiểu
    String weightStr;//Cân nặng (gram)
    String lengthStr;//Chiều dài (cm)
    String widthStr;//Chiều rộng (cm)
    String heightStr;//Chiều cao (cm)
    String shippingFeeStr;//Phí vận chuyển
    Boolean isValid;
    String result;

    public boolean areAllFieldsNull() {
        return productCodeCustomer == null &&
                classifyInfo == null &&
                unitPriceStr == null &&
                stockStr == null &&
                minPurchaseStr == null &&
                weightStr == null &&
                lengthStr == null &&
                widthStr == null &&
                heightStr == null &&
                imageSku == null &&
                shippingFeeStr == null;
    }

    public void setValuesFromStrings() {
        try {
            if (!DataUtils.isNullOrEmpty(unitPriceStr)) {
                setUnitPrice(new BigDecimal(unitPriceStr.trim()));
            }
            // Gán tồn kho (stockStr) vào trường stock (Long)
            if (!DataUtils.isNullOrEmpty(stockStr)) {
                setStock(DataUtils.safeToLong(stockStr.trim()));
            }
            if (!DataUtils.isNullOrEmpty(minPurchaseStr)) {
                setMinPurchase(DataUtils.safeToInt(minPurchaseStr.trim()));
            }

            // Gán cân nặng (weightStr) vào trường weight (Integer)
            if (!DataUtils.isNullOrEmpty(weightStr)) {
                setWeight(DataUtils.safeToInt(weightStr.trim()));
            }
            // Gán và làm tròn chiều dài (lengthStr) chỉ lấy 3 số sau dấu phẩy
            if (!DataUtils.isNullOrEmpty(lengthStr)) {
                setLength(roundToThreeDecimalPlaces(Double.parseDouble(lengthStr.trim())));
            }

            // Gán và làm tròn chiều rộng (widthStr) chỉ lấy 3 số sau dấu phẩy
            if (!DataUtils.isNullOrEmpty(widthStr)) {
                setWidth(roundToThreeDecimalPlaces(Double.parseDouble(widthStr.trim())));
            }

            // Gán và làm tròn chiều cao (heightStr) chỉ lấy 3 số sau dấu phẩy
            if (!DataUtils.isNullOrEmpty(heightStr)) {
                setHeight(roundToThreeDecimalPlaces(Double.parseDouble(heightStr.trim())));
            }
            if(!DataUtils.isNullOrEmpty(classifyInfo)){
                ArrayList<String> classifyInfoList = new ArrayList<>();
                for (String info : classifyInfo.split("-")) {
                    classifyInfoList.add(info.trim()); // Thêm phần tử đã trim vào ArrayList
                }
                setAttribute(classifyInfoList);
            }
            if (!DataUtils.isNullOrEmpty(shippingFeeStr)) {
                setShippingFee(new BigDecimal(shippingFeeStr.trim()));
            }
            if (!DataUtils.isNullOrEmpty(imageSku)) {
                setProductImage(imageSku);
            }
            setActiveStatus(true);
        } catch (Exception e) {
            // Xử lý lỗi nếu xảy ra
            this.isValid = false;
            this.result = "Lỗi trong quá trình gán giá trị: " + e.getMessage();
        }
    }

    // Hàm làm tròn giá trị với 3 số sau dấu phẩy
    private Double roundToThreeDecimalPlaces(Double value) {
        if (value == null) {
            return null;
        }
        BigDecimal bd = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
