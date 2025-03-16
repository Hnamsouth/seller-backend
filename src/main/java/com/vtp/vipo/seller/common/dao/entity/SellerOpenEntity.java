package com.vtp.vipo.seller.common.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "seller_open")
public class SellerOpenEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "int default 10", nullable = true)
    private Integer platformType;

    @Column(length = 255, nullable = true, columnDefinition = "varchar(255) null comment 'Tên shop'")
    private String shopName;

    @Column(length = 255, nullable = true)
    private String originalShopName;

    @Column(length = 255, nullable = true, columnDefinition = "varchar(255) default '0' comment 'Id shop'")
    private String sellerOpenId;

    @Column(columnDefinition = "int default 0", nullable = true)
    private Long merchantId;

    @Lob
    @Column(columnDefinition = "text null comment 'lazbao product.detail.query seller_data_info'")
    private String info;

}
