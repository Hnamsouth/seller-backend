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
public class WithdrawalRequestCreateInfoRes {

    /**
     * Tổng số doanh thu của các đơn đã giao thành công
     */
    BigDecimal totalRevenue;

    /**
     * Số dư khả dụng = Doanh thu trên các đơn đã giao hàng thành công - Lợi nhuận trước thuế của các đơn đã rút.
     */
    BigDecimal availableBalance;

    /**
     * Tài khoản ngân hàng được gắn với tài khoản của người dùng
     */
    String accountInfo;

    BigDecimal taxValue;

}
