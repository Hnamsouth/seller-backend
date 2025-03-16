package com.vtp.vipo.seller.common.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Author: hieuhm12
 * Date: 9/13/2024
 */
@Entity
@Table(name = "product_temporary", indexes = {
        @Index(name = "idx_product", columnList = "productId")
})
@Getter
@Setter
public class ProductTemporaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;
    private String productCodeCustomer;
    @Column(nullable = false)
    private Integer categoryId;

    @Column(length = 250)
    private String name;

    @Column(length = 255)
    private String originalProductName;

    @Column
    private String image;

    @Lob
    private String images;

    @Lob
    private String description;

    @Lob
    private String trailerVideo;

    @Column
    private Integer quoteType;

    @Lob
    private String priceRanges;

    @Column
    private Integer productPriceType;
    @Column
    private Integer minOrderQuantity;

    @Lob
    private String productSpecInfo;
    @Column(precision = 20, scale = 2)
    private BigDecimal displayPrice;
    @Column(precision = 5, scale = 2, columnDefinition = "DECIMAL(5, 2) DEFAULT 0")
    private BigDecimal platformDiscountRate;
    @Column(precision = 20, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(nullable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @LastModifiedDate
    private LocalDateTime updatedDate;
}
