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
@Table(name = "financial_report")
@Entity
public class FinancialReportEntity extends BaseEntity {

    private Long reportId;

    private String periodName;

    private BigDecimal totalRevenue;

    private BigDecimal totalCost;

    private BigDecimal totalProfit;

    private Integer displayOrder;
}
