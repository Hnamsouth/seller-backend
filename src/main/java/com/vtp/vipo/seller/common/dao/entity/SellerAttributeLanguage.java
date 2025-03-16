package com.vtp.vipo.seller.common.dao.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seller_attribute_language")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerAttributeLanguage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sellerAttributeId", nullable = false)
    private Long sellerAttributeId;

    @Column(name = "language", nullable = false, length = 3)
    private String language;

    @Column(name = "attributeName", nullable = false, length = 150)
    private String attributeName;
}
