package com.vtp.vipo.seller.common.enumseller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ActivityType {
    PRICE_ADJUSTMENT,
    ORDER_CANCEL,
    ORDER_REJECT,
    SELLER_APPROVE,
    ORDER_PREPARE,
    ORDER_CONNECT_SHIPMENT,
    PRINT_LABEL
}
