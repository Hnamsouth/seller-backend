package com.vtp.vipo.seller.common.dao.entity.enums.merchant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum ContractStatus {

    NO_CONTRACT(0),                // chưa có hợp đồng
    CONTRACT_ACCEPTED(1),          // đã chấp nhận điều khoản, hợp đồng
    CONTRACT_PENDING_ACCEPTANCE(2),// hợp đồng đã có, chờ người bán chấp nhận
    PENDING(3);                    // chờ xử lý

    int value;

    public static ContractStatus of(Integer value) {
        return switch (value) {
            case 0 -> NO_CONTRACT;
            case 1 -> CONTRACT_ACCEPTED;
            case 2 -> CONTRACT_PENDING_ACCEPTANCE;
            default -> PENDING;
        };
    }
}

