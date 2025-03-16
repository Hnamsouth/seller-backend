package com.vtp.vipo.seller.common.dao.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Author: hieuhm12
 * Date: 9/13/2024
 */
@Entity
@Table(name = "product_seller_sku", indexes = {
        @Index(name = "idx_product", columnList = "productId")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductSellerSkuEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 100)
    private String sellerClassifyId;

    @Column(nullable = false, length = 255)
    private String productImage;

    @Column(precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Long stock;

    private Integer minPurchase;
    private String codeCustomer;
    @Column(nullable = false)
    private String code;
    @Column(nullable = false)
    private Integer weight;

    @Column(precision = 10, scale = 3)
    private BigDecimal length;

    @Column(precision = 10, scale = 3)
    private BigDecimal width;

    @Column(precision = 10, scale = 3)
    private BigDecimal height;

    @Column(precision = 18, scale = 2)
    private BigDecimal shippingFee;

    @Column(nullable = false)
    private Boolean activeStatus = true;

    @Column(nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(nullable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Transient
    private Long tempId;

    /**
     * A flag indicating whether the entity has been deleted. It is not physically deleted from the
     * database, but is marked as deleted with this flag.
     */
    @Column(name = "isDeleted")
    private boolean deleted = false;
}
