package com.vtp.vipo.seller.common.dto.request.product;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.config.validation.annotation.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Objects;
/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
/*Thông số sản phẩm*/
public class ProductSpecInfo {
    @NotNull
    @MinLength(value = 2, errorResponse = ErrorCodeResponse.INVALID_MIN_2_CHAR)
    @MaxLength(value = 100, errorResponse = ErrorCodeResponse.INVALID_MAX_100_CHAR)
    @Regex(pattern = DataUtils.REGEX_NON_SPEC_CHAR, errorResponse = ErrorCodeResponse.INVALID_ALL_SPEC_CHAR)
    @IsTrim
    String parameterName;//Tên thông số
    @NotNull
    @MinLength(value = 2, errorResponse = ErrorCodeResponse.INVALID_MIN_2_CHAR)
    @MaxLength(value = 255, errorResponse = ErrorCodeResponse.INVALID_MAX_255_CHAR)
    @Regex(pattern = DataUtils.REGEX_NON_SPEC_CHAR, errorResponse = ErrorCodeResponse.INVALID_ALL_SPEC_CHAR)
    @IsTrim
    String desc;//Mô tả

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductSpecInfo that = (ProductSpecInfo) o;
        return Objects.equals(parameterName, that.parameterName) &&
                Objects.equals(desc, that.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterName, desc);
    }
}
