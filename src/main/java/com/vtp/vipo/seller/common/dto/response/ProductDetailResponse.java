package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductDetailResponse {

    private String description;

    private String detailVideo;

    private List<String> mainImgUrlList;

    private String mainVideo;

    private int minOrderQuantity;

    private String originalProductName;

    private String originalProductUrl;

    private String originalShopName;

    private int platformType;

    private List<PriceRange> priceRanges;

    private List<ProductAttributeList> productAttributeList;

    private String productId;

    private String productName;

    private int productPriceType;

    private List<ProductProp> productPropList;

    private List<ProductSkuInfo> productSkuInfoList;

    private int quoteType;

    private List<SellerDataInfo> sellerDataInfo;

    private String sellerOpenId;

    private String shopName;

    private SkuPriceRanges skuPriceRanges;

    private int wholesaleMinOrderQuantity;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class SellerDataInfo {

        private double compositeServiceScore;

        private double consultingExperienceScore;

        private double disputeComplaintScore;

        private double logisticsExperienceScore;

        private double offerExperienceScore;

        private int tradeMedalLevel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class PriceRange {

        private double price;

        private int productPriceType;

        private int startQuantity;

        private double systemCurrencyPrice;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class ProductAttributeList {

        private Long attributeId;

        private String attributeName;

        private List<String> attributeValueList;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class ProductProp {

        private String originalPropName;

        private int propId;

        private String propName;

        private List<ValueList> valueList;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class ValueList {

        private String imgUrl;

        private String originalValueName;

        private Long valueId;

        private String valueName;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class ProductSkuInfo {

        private Long height;

        private String imgUrl;

        private Long length;

        private double price;

        private List<PriceRange> priceRanges;

        private long skuId;

        private List<SkuPropList> skuPropList;

        private String specId;

        private int stock;

        private double systemCurrencyPrice;

        private BigDecimal volume;

        private BigDecimal weight;

        private Long width;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class SkuPropList {

        private String originalPropName;

        private String originalValueName;

        private int propId;

        private String propName;

        private Long valueId;

        private String valueName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class SkuPriceRanges {

        private Double maxPrice;

        private Double minPrice;

        private Double systemCurrencyMaxPrice;

        private Double systemCurrencyMinPrice;

    }

}




