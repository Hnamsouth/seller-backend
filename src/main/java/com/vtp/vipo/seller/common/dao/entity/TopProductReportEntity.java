package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "top_product_report")
@Entity
public class TopProductReportEntity extends BaseEntity {

    private Long reportId;

    private Long productId;

    private String productName;

    private String productCode;

    private int quantitySold;

    private BigDecimal revenueBeforeNegotiation;

    private BigDecimal revenueAfterNegotiation;

    private BigDecimal revenuePercentage;

    private int ranking;

    @Builder.Default
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<TopProductItem> skus = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TopProductItem {

        Long skuId;

        String label;

        int quantitySold;

        BigDecimal revenueBeforeNegotiation;

        BigDecimal revenueAfterNegotiation;

        int ranking;
    }
}

