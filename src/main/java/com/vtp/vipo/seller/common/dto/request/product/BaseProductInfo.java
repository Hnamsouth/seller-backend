package com.vtp.vipo.seller.common.dto.request.product;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.config.validation.annotation.*;
import com.vtp.vipo.seller.validator.ValidProductDescriptionHtmlContent;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
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
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
/*Thông tin cơ bản*/
public class BaseProductInfo {
    @NotNull
    @IsTrim
    @MinLength(value = 5, errorResponse = ErrorCodeResponse.INVALID_MIN_5_CHAR)
    @MaxLength(value = 100, errorResponse = ErrorCodeResponse.INVALID_MAX_100_CHAR)
    @Regex(pattern = DataUtils.REGEX_NON_SPEC_CHAR, errorResponse = ErrorCodeResponse.INVALID_ALL_SPEC_CHAR)
    String displayName;             // Tên hiển thị
//    @NotNull
//    @GreaterThan(value = "0", errorResponse = ErrorCodeResponse.INVALID_GREATER_THAN_O)
//    @IsInteger
//    BigDecimal displayPrice;        // Giá hiển thị
    @NotNull
    @IsTrim
    @MinLength(value = 10, errorResponse = ErrorCodeResponse.INVALID_MIN_10_CHAR_PROD_1)
    @MaxLength(value = 255, errorResponse = ErrorCodeResponse.INVALID_MAX_255_CHAR_PROD_1)
    @Regex(pattern = DataUtils.REGEX_NON_SPEC_CHAR, errorResponse = ErrorCodeResponse.INVALID_ALL_SPEC_CHAR_PROD_1)
    String fullName;                // Tên đầy đủ
    //    @NotNull
    Integer categoryId;                // Ngành hàng
    @NotNull
    @MaxListSize(value = 5)
    @IsTrim
    @EndsWith(value = {"jpg", "jpeg", "png",}, ignoreCase = true)
    List<String> productThumbnail;        // Ảnh đại diện của sản phẩm
    @NotNull
    @IsTrim
    @EndsWith(value = {"jpg", "jpeg", "png", "mp4"}, ignoreCase = true)
    List<String> productMedia;      // Ảnh/Video chi tiết sản phẩm

//    @NotNull
//    @IsTrim
//    @MinLength(value = 50, errorResponse = ErrorCodeResponse.INVALID_MIN_50_CHAR)
//    @MaxLength(value = 5000, errorResponse = ErrorCodeResponse.INVALID_MAX_5000_CHAR)
    @ValidProductDescriptionHtmlContent
    String productDescription;      // Mô tả chi tiết sản phẩm

    @Regex(pattern = "^[a-zA-Z0-9\\p{Punct}]+$",errorResponse = ErrorCodeResponse.INVALID_REQUIRED_FIELD)
    @IsTrim
    @MinLength(value = 3, errorResponse = ErrorCodeResponse.INVALID_REQUIRED_FIELD)
    @MaxLength(value = 12, errorResponse = ErrorCodeResponse.INVALID_REQUIRED_FIELD)
    String productCodeCustomer;  //Mã sản phẩm khach hang nhap
    String categoryCode;

    /*Chứng chỉ sản phẩm*/
    Set<CertificateRequest> certificatesInfo;
}
