package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductsResponse {

    private List<Source> source;

    private int totalPage;

    private int currentPage;

    public ProductsResponse(int totalPage, int currentPage) {
        this.totalPage = totalPage;
        this.currentPage = currentPage;
    }

    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Source {

        private String mainImgUrl;

        private Long monthSold;

        private int platformType;

        private double price;

        private String productId;

        private String productName;

        private int productPriceType;

        private String repurchaseRate;

        private double systemCurrencyPrice;

    }
}
