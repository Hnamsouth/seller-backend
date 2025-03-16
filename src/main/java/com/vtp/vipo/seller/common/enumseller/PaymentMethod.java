package com.vtp.vipo.seller.common.enumseller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentMethod {
    OCB_BANK("ocb-bank", "chuyển khoản QR"),
    APPOTA("appota-wallet", "Thẻ/Ví điện tử");

    private final String type;
    private final String message;

    public static String getMessage(String paymentMethod) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.type.equalsIgnoreCase(paymentMethod)) {
                return method.message;
            }
        }
        return null;
    }
}
