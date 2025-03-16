package com.vtp.vipo.seller.common.enumseller;

public enum PrintLabelSortDirection {
    ASC,
    DESC;

    public static boolean contains(String name) {
        for (PrintLabelSortDirection c : PrintLabelSortDirection.values()) {
            if (c.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
