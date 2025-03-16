package com.vtp.vipo.seller.common.constants;

public final class PaymentMethodConstants {

    private PaymentMethodConstants() {
        throw new IllegalStateException(Constants.UTITLITY_CLASS_ERROR);
    }

    public static final String PAYMENT_METHOD_OCB_BANK_OFFLINE = "ocb-bank-offline";

    public static final String DEFAULT_PAYMENT_METHOD = PAYMENT_METHOD_OCB_BANK_OFFLINE;

    public static final String PAYMENT_IN_ADVANCE = "Thanh toán 'Trả trước'";

}
