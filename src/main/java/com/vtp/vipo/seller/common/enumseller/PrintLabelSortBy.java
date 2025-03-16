package com.vtp.vipo.seller.common.enumseller;

public enum PrintLabelSortBy {
    SHIPMENT_CODE,
    ORDER_CODE;

    public static boolean contains(String name) {
        for (PrintLabelSortBy c : PrintLabelSortBy.values()) {
            if (c.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
