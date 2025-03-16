package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.enumseller.ProductReasonType;
import com.vtp.vipo.seller.common.enumseller.ProductStatus;
import com.vtp.vipo.seller.common.utils.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Table(name = "product")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productCode;

    private String productCodeCustomer;

    private String supplier;

    private Integer countryId;

    private String name;

    private String image;

    private String images;

    private String trailerVideo;

    private String description;
    @Column(precision = 20, scale = 2)
    private BigDecimal price;

    private Integer weight = 0;

    private Integer length = 0;

    private Integer width = 0;

    private Integer height = 0;

    private String priceScale;  //json

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private ProductStatus status;

    private Integer activated = 0;

    private Long buyCount;

    private Long merchantId;

    private Integer categoryId = 0;

    private Integer realWeight = 0;

    private Integer isDeleted = 0;

    private Long parentId;

    private Long productPropertyId;

    private String productSkuInfoList;

    private String originalProductName;//ten day du


    private Integer quoteType = 0; //loại mua tối thiểu áp dụng 0 Cộng dồn cả sản phẩm 1: Từng SKU của sản phẩm

    private Long numProdWaitingSold; //Số lượng sản phẩm đang chờ bán

    @Column(nullable = false, updatable = false)
    private Long createTime;

    @Column(nullable = false)
    private Long updateTime;

    @Lob
    private String productSpecInfo; //thong so san pham

    @Column(precision = 5, scale = 2)
    private BigDecimal platformDiscountRate; //% chiết khấu cho sàn

    @Lob
    private String priceRanges; //thang gia sku

    @Column(nullable = false)
    private Integer productPriceType = 0; //Loại giá Loại giá áp dụng 0 Đồng giá SKU không thang giá,1 Đồng giá SKU có thang giá,2 Đơn giá theo SKU

    @Column(nullable = false)
    private Integer minOrderQuantity; //Số lượng tối thiểu

    @Enumerated(EnumType.ORDINAL)
    private ProductReasonType reasonType;

    @Column(length = 255)
    private String reason;

    private String sellerOpenId;

    @Column(precision = 20, scale = 2)
    private BigDecimal displayPrice;

    @PrePersist
    protected void onCreate() {
        createTime = DateUtils.convertMilTimeToSecond(System.currentTimeMillis());
        updateTime = DateUtils.convertMilTimeToSecond(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = DateUtils.convertMilTimeToSecond(System.currentTimeMillis());
    }

}
