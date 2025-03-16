package com.vtp.vipo.seller.common.dto.response.withdrawalrequest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WithdrawalRequestCreateRes {

    /**
     * ID của yêu cầu rút tiền
     */
    String withdrawalRequestId;

    /**
     * Thời gian tạo yêu cầu rút tiền
     */
    LocalDateTime createdAt;

    /**
     * Trạng thái hiện tại của yêu cầu rút tiền {@link WithdrawRequestStatusEnum}
     */
    WithdrawRequestStatusEnum status;

    /**
     * Tổng số tiền yêu cầu rút
     */
    BigDecimal totalAmount;

    /**
     * Số tiền thuế áp dụng đối với yêu cầu rút tiền
     */
    BigDecimal tax;

    /**
     * Danh sách các mục yêu cầu rút tiền có thể rút được (mỗi mục có thể là một gói hàng hoặc sản phẩm cụ thể)
     */
    Collection<WithdrawalRequestItemCreateRes> withdrawableItems;

    /**
     * Ghi chú thêm liên quan đến yêu cầu rút tiền (ví dụ: lý do hoặc yêu cầu đặc biệt)
     */
    String note;

    /**
     * Thông điệp phản hồi hoặc lỗi liên quan đến yêu cầu rút tiền (có thể chứa thông tin về lỗi hoặc kết quả)
     */
    String message;

    /**
     * Lớp bên trong đại diện cho một mục rút tiền, bao gồm thông tin về ID của mục rút tiền, ID của gói hàng và số tiền có thể rút
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WithdrawalRequestItemCreateRes {

        /**
         * ID của mục rút tiền
         */
        String withdrawalItemId;

        /**
         * ID của gói hàng liên quan đến mục rút tiền
         */
        Long packageId;

        /**
         * Số tiền có thể rút từ mục này
         */
        BigDecimal withdrawableAmount;
    }


}
