package com.vtp.vipo.seller.common.dto.response.financial;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.PeriodType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FinancialReportResponse {
    // ID báo cáo doanh thu
    String revenueReportId;

    // Kỳ báo cáo
    PeriodType period;

    // Thời gian bắt đầu
    LocalDate periodStart;

    // Thời gian kết thúc
    LocalDate periodEnd;

    // tổng số lượng đơn hàng
    MetricValue totalOrderPackages;

    // tổng số người mua
    MetricValue totalBuyers;

    // tỷ lệ chuyển đổi
    BigDecimal conversionRate;

    // tỷ lệ đơn đã giao
    BigDecimal deliveryRate;

    // tỷ lệ đơn hoàn hủy
    BigDecimal returnCancelRate;

    // doanh thu
    MetricValue revenue;

    // tổng phí điều chỉnh giá
    BigDecimal totalPriceNegotiated;

    // lợi nhuận
    MetricValue totalProfit;

    // Phí sàn
    BigDecimal platformFee;

    // Chi phí khác
    BigDecimal otherFee;

    // Mảng báo cáo về các sản phẩm
    @Builder.Default
    List<TopProduct> topProductReport = new ArrayList<>();

    // Dữ liệu cho biểu đồ
    ChartData chartData;

    // Mảng chứa thông tin về biểu đồ tổng hợp doanh thu, chi phí, lợi nhuận
    @Builder.Default
    List<FinancialDataItem> financialData = new ArrayList<>();

    // Dữ liệu cho tổng lượng truy cập
    TotalVisitsData totalVisitsData;

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TopProduct {

        // ID sản phẩm
        Long productId;

        // Tên sản phẩm
        String productName;

        // Mã sản phẩm
        String productCode;

        // số lượng
        int quantitySold;

        // Thành tiền trước đàm phán
        BigDecimal revenueBeforeNegotiation;

        // Thành tiền sau đàm phán
        BigDecimal revenueAfterNegotiation;

        // % doanh thu
        BigDecimal revenuePercentage;

        // thứ hạng
        int ranking;

        // Danh sách sản phẩm
        List<TopProductItem> skus;
    }

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TopProductItem {

        // ID sản phẩm
        Long skuId;

        // Tên sản phẩm theo sku -> Size M, Màu Trắng
        String label;

        // số lượng
        int quantitySold;

        // Thành tiền trước đàm phán
        BigDecimal revenueBeforeNegotiation;

        // Thành tiền sau đàm phán
        BigDecimal revenueAfterNegotiation;

        // thứ hạng
        int ranking;
    }

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ChartData {
        BigDecimal totalRevenue;

        List<ChartDataItem> data;
    }

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ChartDataItem {
        String label;

        BigDecimal percentage;

        int ranking;

        @JsonIgnore
        int displayOrder;
    }

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class FinancialDataItem {

        // Tên hiển thị khoảng thời gian
        String periodName;

        // Tổng doanh thu
        BigDecimal totalRevenue;

        // Tổng phí
        BigDecimal totalCost;

        // Tổng lợi nhuận
        BigDecimal totalProfit;

        // Thứ tự hiển thị
        @JsonIgnore
        int displayOrder;
    }

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TotalVisitsData {

        // Tổng lượt truy cập
        Long totalVisits;

        // Thời gian cập nhật mới nhất
        Long updatedAt;
    }
}
