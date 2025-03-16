package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.BaseEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.PeriodType;
import com.vtp.vipo.seller.common.dto.response.financial.MetricValue;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDate;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "revenue_report")
@Entity
public class RevenueReportEntity extends BaseEntity {

    // Thời điểm bắt đầu báo cáo (DATE)
    private LocalDate periodStart;

    // Thời điểm kết thúc báo cáo (DATE)
    private LocalDate periodEnd;

    // Loại báo cáo
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    // ID của product
    @Column(columnDefinition = "json")
    private String productIds;

    // Tổng số lượng đơn hàng
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private MetricValue totalOrderPackages;

    // Tổng số người mua
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private MetricValue totalBuyers;

    // Tỷ lệ chuyển đổi
    private BigDecimal conversionRate;

    // Tỷ lệ đơn đã giao
    private BigDecimal deliveryRate;

    // Tỷ lệ đơn hoàn hủy
    private BigDecimal returnCancelRate;

    // Doanh thu
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private MetricValue revenue;

    // Tổng phí điều chỉnh giá
    private BigDecimal totalPriceNegotiated;

    // Lợi nhuận
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private MetricValue totalProfit;

    // Tổng phí sàn
    private BigDecimal totalPlatformFee;

    // Tổng phí khác
    private BigDecimal totalOtherFees;

    // ID của seller
    private Long merchantId;
}
