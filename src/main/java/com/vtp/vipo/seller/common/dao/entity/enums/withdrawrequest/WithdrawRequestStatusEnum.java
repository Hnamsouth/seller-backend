package com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum WithdrawRequestStatusEnum {

    PENDING("Chờ xử lý"),

    PROCESSING("Đang xử lý"),

    APPROVED("Đã phê duyệt"),

    REJECTED("Từ chối"),

    SUCCESS("Thành công"),

    CANCELED("Hủy");

    String lable;

    public static String getLableFromEnum(WithdrawRequestStatusEnum status) {
        return status.lable;
    }

}
