package com.vtp.vipo.seller.common.dao.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "order_package_split")
@Entity
@Getter
@Setter
public class OrderPackageSplitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long packageId;

    @Column(nullable = false)
    private String subpackageCode;  //domestic_package_id

    private String logisticsCode;

    private String logisticsNo;

    @Column(nullable = false)
    private String status;

    private String statusDesc;

    @Column(nullable = false)
    private Float weight;

    @Column(nullable = false)
    private Float height;

    @Column(nullable = false)
    private Float width;

    @Column(nullable = false)
    private Float length;

    @Column(nullable = false)
    private String source;

}
