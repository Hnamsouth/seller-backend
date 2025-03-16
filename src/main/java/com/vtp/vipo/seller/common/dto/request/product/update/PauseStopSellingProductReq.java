package com.vtp.vipo.seller.common.dto.request.product.update;

import com.vtp.vipo.seller.common.enumseller.ProductReasonType;
import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.config.validation.annotation.MaxLength;
import com.vtp.vipo.seller.config.validation.annotation.MinLength;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;
import com.vtp.vipo.seller.config.validation.annotation.OneOf;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: hieuhm12
 * Date: 9/18/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PauseStopSellingProductReq {
    /*
    0: Hết hàng tạm thời (Mặc định chọn)
    1: Cần bảo trì/sửa chữa
    2: Cần điều chỉnh giá
    3: Tạm dừng bán theo mùa
    4: Chất lượng NCC không đảm bảo
    5: Khác - Tạm dừng bán

Ngừng bán:
    6: Ngừng kinh doanh (Mặc định chọn)
    7: Ngừng sản xuất
    8: Sản phẩm lỗi
    9: Chất lượng NCC không đảm bảo
    10: Khác - Ngừng bán

Xóa sản phẩm:
    11: Sản phẩm bị lỗi nhập thông tin.
    12: Sản phẩm bị trùng lặp
    13: Ngừng kinh doanh sản phẩm
    14: Không phù hợp với thị trường
    15: Thay đổi chiến lược kinh doanh
    16: Yêu cầu từ nhà sản xuất hoặc nhà cung cấp
    17: Khác - Xóa sản phẩm
*/
    @NotNull
    Long id;
    @NotNull
    @OneOf(collection = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
    "11","12","13","14","15","16","17"}, errorResponse = ErrorCodeResponse.INVALID_REQUIRED_FIELD)
    Integer reasonType;
    @MinLength(value = 20, errorResponse = ErrorCodeResponse.INVALID_REQUIRED_FIELD)
    @MaxLength(value = 250, errorResponse = ErrorCodeResponse.INVALID_REQUIRED_FIELD)
    String reason;

    public void validate() {
        if ((ProductReasonType.fromValue(reasonType).equals(ProductReasonType.OTHER_PAUSE)
                || ProductReasonType.fromValue(reasonType).equals(ProductReasonType.OTHER_STOP)
                || ProductReasonType.fromValue(reasonType).equals(ProductReasonType.OTHER_DELETE))
                && DataUtils.isNullOrEmpty(reason)) {
            throw new VipoBusinessException(ErrorCodeResponse.REQUIRED_FIELD,"reason");
        }
    }
}
