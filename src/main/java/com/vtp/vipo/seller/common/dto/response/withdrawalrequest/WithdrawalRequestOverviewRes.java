package com.vtp.vipo.seller.common.dto.response.withdrawalrequest;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WithdrawalRequestOverviewRes {

    /**
     * Tổng số tiền của các đơn có trạng thái hủy, từ chối và chưa tạo yêu cầu
     */
    BigDecimal balancePending;

    /**
     * Tổng số tiền rút trong tuần này
     */
    BigDecimal withdrawalThisWeek;

    /**
     * Tổng số tiền rút trong tháng này
     */
    BigDecimal withdrawalThisMonth;

    /**
     * Tổng số tiền đã rút
     */
    BigDecimal totalWithdrawn;

    /**
     * Số lần rút còn lại
     */
    String remainingWithdrawals;

    /**
     * Tài khoản ngân hàng được gắn với tài khoản của người dùng
     */
    String accountInfo;

    /**
     * Có thể tạo yêu cầu không
     */
    Boolean canCreateRequest;

    Long date;

}
