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

import java.lang.reflect.Field;
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
public class ProductCreateByFileExcel extends ProductCreateUpdateRequest {
    private static final Logger log = LoggerFactory.getLogger(ProductCreateByFileExcel.class);
    // Mã sản phẩm
    private String productCodeStr;            // Mã duy nhất của sản phẩm

    // Tên hiển thị
    private String displayNameStr;            // Tên sản phẩm hiển thị trên hệ thống

    // Tên đầy đủ
    private String fullNameStr;               // Tên đầy đủ của sản phẩm

    // Ngành hàng
    private String categoryIdStr;               // Danh mục (ngành hàng) của sản phẩm

    // Ảnh đại diện sản phẩm
    private String productThumbnailStr;       // URL ảnh đại diện của sản phẩm

    // Ảnh/Video chi tiết 1-5
    private String productMedia1Str;          // URL ảnh hoặc video chi tiết 1 của sản phẩm
    private String productMedia2Str;          // URL ảnh hoặc video chi tiết 2 của sản phẩm
    private String productMedia3Str;          // URL ảnh hoặc video chi tiết 3 của sản phẩm
    private String productMedia4Str;          // URL ảnh hoặc video chi tiết 4 của sản phẩm
    private String productMedia5Str;          // URL ảnh hoặc video chi tiết 5 của sản phẩm

    // Mô tả sản phẩm
    private String productDescriptionStr;     // Mô tả chi tiết về sản phẩm

    // Chiết khấu sàn
    private String platformDiscountRateStr;   // Tỷ lệ chiết khấu của sàn (platform)

    // Loại giá
    private String priceTypeStr;              // Loại giá của sản phẩm (ví dụ: bán lẻ, bán sỉ)

    // Giá áp dụng
    private String appliedPriceStr;           // Giá tiền hiện tại của sản phẩm

    // Loại mua tối thiểu
    private String minPurchaseTypeStr;        // Loại yêu cầu mua tối thiểu (ví dụ: theo số lượng hoặc đơn vị)

    // Tối thiểu cộng đồn sản phẩm
    private String minPurchaseQuantityStr;    // Số lượng tối thiểu yêu cầu khi mua sản phẩm

    // Tên thông số 1-5 và mô tả thông số 1-5
    private String parameterName1;         // Tên thông số kỹ thuật 1 của sản phẩm
    private String parameterDesc1;         // Mô tả thông số kỹ thuật 1 của sản phẩm
    private String parameterName2;         // Tên thông số kỹ thuật 2 của sản phẩm
    private String parameterDesc2;         // Mô tả thông số kỹ thuật 2 của sản phẩm
    private String parameterName3;         // Tên thông số kỹ thuật 3 của sản phẩm
    private String parameterDesc3;         // Mô tả thông số kỹ thuật 3 của sản phẩm
    private String parameterName4;         // Tên thông số kỹ thuật 4 của sản phẩm
    private String parameterDesc4;         // Mô tả thông số kỹ thuật 4 của sản phẩm
    private String parameterName5;         // Tên thông số kỹ thuật 5 của sản phẩm
    private String parameterDesc5;         // Mô tả thông số kỹ thuật 5 của sản phẩm

    // Thuộc tính 1-3 và phân loại cho thuộc tính 1-3
    private String attribute1;             // Thuộc tính tùy chỉnh 1 của sản phẩm
    private String classify1;              // Phân loại chi tiết cho thuộc tính 1
    private String attribute2;             // Thuộc tính tùy chỉnh 2 của sản phẩm
    private String classify2;              // Phân loại chi tiết cho thuộc tính 2
    private String attribute3;             // Thuộc tính tùy chỉnh 3 của sản phẩm
    private String classify3;              // Phân loại chi tiết cho thuộc tính 3

    // Giá tiền thang giá 1-3 và số lượng bắt đầu thang giá 2-3
    private String priceStep1;             // Giá áp dụng cho thang giá 1
    private String quantityStep2;          // Số lượng sản phẩm yêu cầu để áp dụng thang giá 2
    private String priceStep2;             // Giá áp dụng cho thang giá 2
    private String quantityStep3;          // Số lượng sản phẩm yêu cầu để áp dụng thang giá 3
    private String priceStep3;             // Giá áp dụng cho thang giá 3
    private Boolean isValid;
    private String result;

    public void setValuesFromStrings() {
        if (Boolean.TRUE.equals(isValid)) {
            try {
                BaseProductInfo baseProductInfo = new BaseProductInfo();
                baseProductInfo.setDisplayName(this.displayNameStr);
                baseProductInfo.setFullName(this.fullNameStr);
                // Chuyển đổi categoryId sang Integer
                baseProductInfo.setCategoryCode(this.categoryIdStr);
                // Chuyển đổi productThumbnail sang danh sách List<String>
                baseProductInfo.setProductThumbnail(convertToList(this.productThumbnailStr));
                // Chuyển đổi productMedia sang danh sách List<String>
                baseProductInfo.setProductMedia(convertToList(this.productMedia1Str, this.productMedia2Str, this.productMedia3Str, this.productMedia4Str, this.productMedia5Str));
                // Chuyển đổi productDescription
                baseProductInfo.setProductDescription(this.productDescriptionStr);
                setBaseProductInfo(baseProductInfo);
                SellingProductInfo sellingProductInfo = new SellingProductInfo();
                // Chuyển đổi platformDiscountRate sang BigDecimal
                sellingProductInfo.setPlatformDiscountRate(convertToBigDecimal(this.platformDiscountRateStr));
                // Chuyển đổi priceType sang Integer
                sellingProductInfo.setPriceType(DataUtils.safeToInt(this.priceTypeStr));
                // Chuyển đổi appliedPrice sang BigDecimal
                sellingProductInfo.setProductPrice(convertToBigDecimal(this.appliedPriceStr));
                // Chuyển đổi minPurchaseType và minPurchaseQuantity
                sellingProductInfo.setMinPurchaseType(DataUtils.safeToInt(this.minPurchaseTypeStr));
                sellingProductInfo.setMinPurchaseQuantity(DataUtils.safeToInt(this.minPurchaseQuantityStr));
                setSellingProductInfo(sellingProductInfo);
                setProductSpecInfo(convertToProductSpecInfoList());
                setProductAttributesInfo(convertToProductAttributesInfoSet());
                if (DataUtils.safeToString(FIXED_PRICE_SKU_WITH_PRICE_STEP).equals(priceTypeStr)) {
                    setStepPriceInfo(convertToStepPriceInfoSet(priceStep1, quantityStep2,
                            priceStep2, quantityStep3, priceStep3, sellingProductInfo.getMinPurchaseQuantity()));
                }
            } catch (Exception e) {
                log.error(StackTraceUtil.stackTrace(e));
            }
        }
    }

    private List<String> convertToList(String... values) {
        List<String> resultList = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                resultList.add(value);
            }
        }
        return resultList;
    }

    private BigDecimal convertToBigDecimal(String value) {
        if(DataUtils.isNullOrEmpty(value)) return null;
        try {
            return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.error(StackTraceUtil.stackTrace(e));
            return null;
        }
    }

    public Set<ProductSpecInfo> convertToProductSpecInfoList() {
        Set<ProductSpecInfo> productSpecInfos = new HashSet<>();
        if (!DataUtils.isNullOrEmpty(parameterName1) && !DataUtils.isNullOrEmpty(parameterName1.trim())
                && !DataUtils.isNullOrEmpty(parameterDesc1) && !DataUtils.isNullOrEmpty(parameterDesc1.trim())) {
            productSpecInfos.add(new ProductSpecInfo(parameterName1, parameterDesc1));
        }
        if (!DataUtils.isNullOrEmpty(parameterName2) && !DataUtils.isNullOrEmpty(parameterName2.trim())
                && !DataUtils.isNullOrEmpty(parameterDesc2) && !DataUtils.isNullOrEmpty(parameterDesc2.trim())) {
            productSpecInfos.add(new ProductSpecInfo(parameterName2, parameterDesc2));
        }
        if (!DataUtils.isNullOrEmpty(parameterName3) && !DataUtils.isNullOrEmpty(parameterName3.trim())
                && !DataUtils.isNullOrEmpty(parameterDesc3) && !DataUtils.isNullOrEmpty(parameterDesc3.trim())) {
            productSpecInfos.add(new ProductSpecInfo(parameterName3, parameterDesc3));
        }
        if (!DataUtils.isNullOrEmpty(parameterName4) && !DataUtils.isNullOrEmpty(parameterName4.trim())
                && !DataUtils.isNullOrEmpty(parameterDesc4) && !DataUtils.isNullOrEmpty(parameterDesc4.trim())) {
            productSpecInfos.add(new ProductSpecInfo(parameterName4, parameterDesc4));
        }
        if (!DataUtils.isNullOrEmpty(parameterName5) && !DataUtils.isNullOrEmpty(parameterName5.trim())
                && !DataUtils.isNullOrEmpty(parameterDesc5) && !DataUtils.isNullOrEmpty(parameterDesc5.trim())) {
            productSpecInfos.add(new ProductSpecInfo(parameterName5, parameterDesc5));
        }
        return productSpecInfos;
    }

    public Set<ProductAttributesInfo> convertToProductAttributesInfoSet() {
        Set<ProductAttributesInfo> productAttributesInfos = new LinkedHashSet<>();
        int stt = 1; // Bắt đầu từ 1
        // Kiểm tra attribute1 và classify1
        if (!DataUtils.isNullOrEmpty(attribute1) && !DataUtils.isNullOrEmpty(classify1)) {
            LinkedHashMap<String, String> nameAndImage1 = new LinkedHashMap<>();
            if(!DataUtils.isNullOrEmpty(classify1)){
                for (String info : classify1.split(";")) {
                    nameAndImage1.put(info.trim(),"");
                }
            }
            productAttributesInfos.add(new ProductAttributesInfo(attribute1, nameAndImage1, stt++));
        }
        // Kiểm tra attribute2 và classify2
        if (!DataUtils.isNullOrEmpty(attribute2) && !DataUtils.isNullOrEmpty(classify2)) {
            LinkedHashMap<String, String> nameAndImage2 = new LinkedHashMap<>();
            if(!DataUtils.isNullOrEmpty(classify2)){
                for (String info : classify2.split(";")) {
                    nameAndImage2.put(info.trim(),"");
                }
            }
            productAttributesInfos.add(new ProductAttributesInfo(attribute2, nameAndImage2, stt++));
        }
        // Kiểm tra attribute3 và classify3
        if (!DataUtils.isNullOrEmpty(attribute3) && !DataUtils.isNullOrEmpty(classify3)) {
            LinkedHashMap<String, String> nameAndImage3 = new LinkedHashMap<>();
            if(!DataUtils.isNullOrEmpty(classify3)){
                for (String info : classify3.split(";")) {
                    nameAndImage3.put(info.trim(),"");
                }
            }
            productAttributesInfos.add(new ProductAttributesInfo(attribute3, nameAndImage3, stt++));
        }
        return productAttributesInfos;
    }

    public Set<StepPriceInfo> convertToStepPriceInfoSet(String priceStep1, String quantityStep2,
                                                        String priceStep2, String quantityStep3,
                                                        String priceStep3, Integer minPurchaseQuantity) {
        Set<StepPriceInfo> stepPriceSet = new LinkedHashSet<>();
        Integer toQuantityLastIndex = Integer.parseInt(quantityStep2) - 1;
        // Convert và thêm thang giá 1
        if (!DataUtils.isNullOrEmpty(priceStep1)) {
            BigDecimal price1 = new BigDecimal(priceStep1);
            Integer fromQuantity = !DataUtils.isNullOrEmpty(minPurchaseQuantity) ? minPurchaseQuantity : 1;
            stepPriceSet.add(new StepPriceInfo(1, fromQuantity, toQuantityLastIndex, price1));
        }

        // Convert và thêm thang giá 2
        if (!DataUtils.isNullOrEmpty(priceStep2)
                && !DataUtils.isNullOrEmpty(quantityStep2)) {
            BigDecimal price2 = new BigDecimal(priceStep2);
            Integer fromQuantity2 = toQuantityLastIndex + 1;
            toQuantityLastIndex = Integer.parseInt(quantityStep3) - 1;
            stepPriceSet.add(new StepPriceInfo(2, fromQuantity2, toQuantityLastIndex, price2));
        }

        // Convert và thêm thang giá 3
        if (!DataUtils.isNullOrEmpty(priceStep3)
                && !DataUtils.isNullOrEmpty(quantityStep3)) {
            BigDecimal price3 = new BigDecimal(priceStep3);
            Integer fromQuantity3 = toQuantityLastIndex + 1;
            stepPriceSet.add(new StepPriceInfo(3, fromQuantity3, null, price3));
        }

        return stepPriceSet;
    }

    public boolean isAllFieldsNullOrEmpty() {
        return DataUtils.isNullOrEmpty(productCodeStr) &&
                DataUtils.isNullOrEmpty(displayNameStr) &&
                DataUtils.isNullOrEmpty(fullNameStr) &&
                DataUtils.isNullOrEmpty(categoryIdStr) &&
                DataUtils.isNullOrEmpty(productThumbnailStr) &&
                DataUtils.isNullOrEmpty(productMedia1Str) &&
                DataUtils.isNullOrEmpty(productMedia2Str) &&
                DataUtils.isNullOrEmpty(productMedia3Str) &&
                DataUtils.isNullOrEmpty(productMedia4Str) &&
                DataUtils.isNullOrEmpty(productMedia5Str) &&
                DataUtils.isNullOrEmpty(productDescriptionStr) &&
                DataUtils.isNullOrEmpty(platformDiscountRateStr) &&
                DataUtils.isNullOrEmpty(priceTypeStr) &&
                DataUtils.isNullOrEmpty(appliedPriceStr) &&
                DataUtils.isNullOrEmpty(minPurchaseTypeStr) &&
                DataUtils.isNullOrEmpty(minPurchaseQuantityStr) &&
                DataUtils.isNullOrEmpty(parameterName1) &&
                DataUtils.isNullOrEmpty(parameterDesc1) &&
                DataUtils.isNullOrEmpty(parameterName2) &&
                DataUtils.isNullOrEmpty(parameterDesc2) &&
                DataUtils.isNullOrEmpty(parameterName3) &&
                DataUtils.isNullOrEmpty(parameterDesc3) &&
                DataUtils.isNullOrEmpty(parameterName4) &&
                DataUtils.isNullOrEmpty(parameterDesc4) &&
                DataUtils.isNullOrEmpty(parameterName5) &&
                DataUtils.isNullOrEmpty(parameterDesc5) &&
                DataUtils.isNullOrEmpty(attribute1) &&
                DataUtils.isNullOrEmpty(classify1) &&
                DataUtils.isNullOrEmpty(attribute2) &&
                DataUtils.isNullOrEmpty(classify2) &&
                DataUtils.isNullOrEmpty(attribute3) &&
                DataUtils.isNullOrEmpty(classify3) &&
                DataUtils.isNullOrEmpty(priceStep1) &&
                DataUtils.isNullOrEmpty(quantityStep2) &&
                DataUtils.isNullOrEmpty(priceStep2) &&
                DataUtils.isNullOrEmpty(quantityStep3) &&
                DataUtils.isNullOrEmpty(priceStep3);
    }


}
