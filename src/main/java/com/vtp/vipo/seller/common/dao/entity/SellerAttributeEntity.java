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
@Table(name = "seller_attribute", indexes = {
        @Index(name = "idx_product", columnList = "productId")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SellerAttributeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 150)
    private String attributeName;

    @Column(nullable = false)
    private int attributeOrder;

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

    public SellerAttributeEntity(Long id, Long productId, String attributeName, int attributeOrder, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.productId = productId;
        this.attributeName = attributeName;
        this.attributeOrder = attributeOrder;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

}
