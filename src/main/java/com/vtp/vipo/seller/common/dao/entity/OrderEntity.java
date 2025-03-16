package com.vtp.vipo.seller.common.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "`order`")
@Setter
@Getter
@NoArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private String code;

    private String paymentMethod;

    private BigDecimal totalPaymentAmount;

    private Long paymentTime;

    private Long createTime;

    private Long updateTime;

    private Long merchantIds;

    private String buyerPhone;

    private String buyerName;

    private String buyerAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrderPackageEntity> orderPackageEntities;

}
