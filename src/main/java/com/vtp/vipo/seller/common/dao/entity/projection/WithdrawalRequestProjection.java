package com.vtp.vipo.seller.common.dao.entity.projection;

import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface WithdrawalRequestProjection {

    Long getId();

    LocalDateTime getDate();

    WithdrawalRequestType getWithdrawalRequestType();

    WithdrawRequestStatusEnum getWithdrawalRequestStatus();

    String getReason();

    BigDecimal getAmount();

    String getBankCode();

    String getAccountNumber();

    String getAccountName();

    String getBankBranch();

    BigDecimal getTax();

    BigDecimal getTaxValue();

    BigDecimal getTotalWithdrawal();

    Integer getReCreated();

}
