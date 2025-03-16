package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.enums.PlatformFeeStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.PlatformFeeType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "platform_fee")
public class PlatformFeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long merchantGroupId;

    @Enumerated(EnumType.STRING)
    private PlatformFeeType type;

    private String name;

    private String description;

    private BigDecimal value;

    private BigDecimal minPrice;

    @Enumerated(EnumType.STRING)
    private PlatformFeeStatus status;

    @Column(name = "createdAt")
    private Long createdAt;

    @Column(name = "updatedAt")
    private Long updatedAt;

}
