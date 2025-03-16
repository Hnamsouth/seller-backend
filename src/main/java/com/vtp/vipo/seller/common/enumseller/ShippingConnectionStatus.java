package com.vtp.vipo.seller.common.enumseller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ShippingConnectionStatus {
    SUCCESS,
    PENDING,
    FAIL
}
