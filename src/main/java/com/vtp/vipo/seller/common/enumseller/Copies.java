package com.vtp.vipo.seller.common.enumseller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Copies {
    ONE_SHEET(1, "1 liên"),
    TWO_SHEET(2, "2 liên");

    final Integer value;
    final String description;

    public static boolean contains(String name) {
        for (Copies c : Copies.values()) {
            if (c.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
