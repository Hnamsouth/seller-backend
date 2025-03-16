package com.vtp.vipo.seller.common.dao.entity.enums.merchant;

import com.vtp.vipo.seller.common.exception.VipoInvalidDataRequestException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum MerchantContractType {

    NO_CONTRACT(0),
    PERSONAL(1),
    BUSINESS(2);

    int value;

    public static MerchantContractType of(Integer value) {
        return switch (value) {
            case 0 -> NO_CONTRACT;
            case 1 -> PERSONAL;
            case 2 -> BUSINESS;
            default -> null;
        };
    }

}
