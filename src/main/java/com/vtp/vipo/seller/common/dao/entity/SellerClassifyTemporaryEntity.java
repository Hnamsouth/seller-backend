package com.vtp.vipo.seller.common.dao.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Author: hieuhm12
 * Date: 9/13/2024
 */
@Entity
@Table(name = "seller_classify_temporary", indexes = {
        @Index(name = "idx_product", columnList = "productTemporaryId")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SellerClassifyTemporaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productTemporaryId;

    @Column(nullable = false)
    private Long sellerAttributeTemporaryId;

    @Column(length = 255)
    private String sellerImage;

    @Column(nullable = false, length = 150)
    private String sellerName;

    @Column(nullable = false)
    private int orderClassify;

    @Column(nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(nullable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @LastModifiedDate
    private LocalDateTime updatedDate;

    private Long sellerClassifyId;

    @Transient
    private String tempId;
}
