package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "revenue_fee_report")
@Entity
public class RevenueFeeReportEntity extends BaseEntity {

    private Long reportId;

    private Long platformFeeId;

    private String feeName;

    private BigDecimal totalFee;
}

