package com.vtp.vipo.seller.common.dto.request.product;

import com.vtp.vipo.seller.common.dto.response.product.detail.SellerClassifyInfo;
import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.config.validation.annotation.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
/*Thông tin thuộc tính*/
public class ProductAttributesInfo {
    //Tên thuộc tính (Tên thuộc tính là duy nhất)
    @NotNull
    @IsTrim
    @MinLength(value = 2, errorResponse = ErrorCodeResponse.INVALID_MIN_2_CHAR)
    @MaxLength(value = 150, errorResponse = ErrorCodeResponse.INVALID_MAX_150_CHAR)
    @Regex(pattern = DataUtils.REGEX_NON_SPEC_CHAR, errorResponse = ErrorCodeResponse.INVALID_ALL_SPEC_CHAR)
    String attributeName;

    //Tên phân loại (unique)
    @NotNull
    @IsTrim
    LinkedHashMap<String, String> nameAndImage;

    /* Phase 5.5: Product Approval Fix:
     * store the response for each seller_classify */
    List<SellerClassifyInfo> sellerClassifyInfos = new ArrayList<>();

    @NotNull
    int stt;//t dau tu 1 va tang dan 1 don vi

    Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductAttributesInfo that = (ProductAttributesInfo) o;
        return Objects.equals(attributeName, that.attributeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeName);
    }

    public void validate() {
        for (Map.Entry<String, String> entry : this.nameAndImage.entrySet()) {
            String key = entry.getKey();
            if (key.length() < 2) {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_MIN_2_CHAR);
            }
            if (key.length() > 150) {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_MAX_150_CHAR);
            }
            Pattern p = Pattern.compile(DataUtils.REGEX_NON_SPEC_CHAR);
            Matcher matcher = p.matcher(key);
            if (!matcher.matches()) {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_ALL_SPEC_CHAR);
            }
        }
    }

    public List<String> getImage() {
        return new ArrayList<>(this.nameAndImage.values());
    }

    public List<String> getNameClassify() {
        return new ArrayList<>(this.nameAndImage.keySet());

    }

    public boolean isHaveImage() {
        int countImage = 0;
        for (Map.Entry<String, String> entry : this.nameAndImage.entrySet()) {
            String value = entry.getValue();
            if (!DataUtils.isNullOrEmpty(value)) countImage++;
        }
        return countImage > 0;
    }

    public ProductAttributesInfo(String attributeName, LinkedHashMap<String, String> nameAndImage, int stt) {
        this.attributeName = attributeName;
        this.nameAndImage = nameAndImage;
        this.stt = stt;
    }

}
