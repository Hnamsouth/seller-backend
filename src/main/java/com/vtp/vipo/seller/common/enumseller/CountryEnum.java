package com.vtp.vipo.seller.common.enumseller;

public enum CountryEnum {

    VIETNAM("VN", "Vietnam"),
    THAILAND("TH", "Thailand"),
    MALAYSIA("MY", "Malaysia"),
    INDONESIA("ID", "Indonesia"),
    PHILIPPINES("PH", "Philippines"),
    SINGAPORE("SG", "Singapore"),
    MYANMAR("MM", "Myanmar"),
    CAMBODIA("KH", "Cambodia"),
    LAOS("LA", "Laos"),
    BRUNEI("BN", "Brunei"),
    TIMOR_LESTE("TL", "Timor-Leste");
    //todo: add more countries

    private final String code;
    private final String name;

    CountryEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}