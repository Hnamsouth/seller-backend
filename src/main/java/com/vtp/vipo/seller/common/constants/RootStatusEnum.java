package com.vtp.vipo.seller.common.constants;

import lombok.Getter;
@Getter
public enum RootStatusEnum {

    WAIT_FOR_PAY("1", "Chờ thanh toán"),

    WAIT_FOR_PROCESS("2", "Chờ xử lí"),

    WAIT_FOR_DELIVERY("3", "Chờ giao hàng"),

    DELIVERING("4", "Đang giao hàng"),

    SUCCESSFUL_DELIVERY("5", "Đã giao hàng"),

    CANCELLED("6", "Đã hủy"),

    REFUND("7", "Hoàn hàng");

    private final String code;

    private final String name;

    RootStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
