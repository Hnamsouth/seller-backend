package com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest;

import java.util.List;

public enum WithdrawalRequestExportEnum {

    PENDING,
    COMPLETED,
    FAILED;

    public final static List<WithdrawalRequestExportEnum> NOT_FAILED_WITHDRAL_REQUEST_STATUSES
            = List.of(COMPLETED, PENDING);

}
