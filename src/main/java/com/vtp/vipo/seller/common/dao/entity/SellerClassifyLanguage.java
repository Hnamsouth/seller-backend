package com.vtp.vipo.seller.common.dao.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seller_classify_language")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerClassifyLanguage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sellerClassifyId", nullable = false)
    private Long sellerClassifyId;

    @Column(name = "language", nullable = false, length = 3)
    private String language;

    @Column(name = "classifyName", nullable = false, length = 150)
    private String classifyName;
}
