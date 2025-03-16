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
@Table(name = "chart_top_product_report")
@Entity
public class ChartTopProductReportEntity extends BaseEntity {

    private Long reportId;

    private String label;

    private BigDecimal percentage;

    private int ranking;

    private Integer displayOrder;

    private BigDecimal totalRevenue;
}
