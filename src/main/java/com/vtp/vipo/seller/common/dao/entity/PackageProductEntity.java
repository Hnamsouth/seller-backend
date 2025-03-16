package com.vtp.vipo.seller.common.dao.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Table(name = "package_product")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class PackageProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name ="originalSpecMap")
    private String originalItemSpecMap;

    private String specMap;

    private String name;

    @Column(name = "merchantProductId")
    private String productId;

    private Integer productSource;

    private String productUrl;

    private String originalProductName;

    @Column(name = "image")
    private String skuImageUrl;

    @Column(name = "price")
    private BigDecimal skuPrice;

    @Column(name = "priceRMB")
    private BigDecimal skuPriceRMB;

    private Long customerId;

    @Column(nullable = false)
    private Long quantity;

    private String priceRanges;

    private String priceRangesSku;

    private String productPriceType;

    private Integer quoteType; //small int in mysql, need to check

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packageId", referencedColumnName = "id", nullable=false)
    @JsonManagedReference
    @JsonIgnore
    private OrderPackageEntity orderPackage;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", referencedColumnName = "id", nullable=false)
    private OrderEntity order;

    private BigDecimal negotiatedAmount;

    private String lazbaoSkuId; //store the sku id in both Lazbao and seller product

    private Long merchantId;

    private String sellerOpenId;

    private BigDecimal totalPrice;

    private Long weight;

    private BigDecimal sellerPlatformDiscountRate;

    private BigDecimal sellerPlatformDiscountAmount;
}
