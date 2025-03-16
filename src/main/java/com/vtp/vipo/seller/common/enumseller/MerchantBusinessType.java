package com.vtp.vipo.seller.common.enumseller;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;

public enum MerchantBusinessType {
    HOUSEHOLD_BUSINESS(0),
    ENTERPRISE(1);

    private final int value;

    MerchantBusinessType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MerchantBusinessType fromValue(int value) {
        for (MerchantBusinessType type : MerchantBusinessType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new VipoBusinessException(ErrorCodeResponse.COMMON_NOT_FOUND_ID, String.valueOf(value));
    }
}
