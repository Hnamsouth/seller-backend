package com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawalRequestType {

    WITHDRAW_TO_ACCOUNT("Tạo yêu cầu rút tiền về số tài khoản");

    private final String label;
}
