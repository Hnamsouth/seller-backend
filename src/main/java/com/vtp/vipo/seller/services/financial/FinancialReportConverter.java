package com.vtp.vipo.seller.services.financial;

import com.vtp.vipo.seller.common.dao.entity.ChartTopProductReportEntity;
import com.vtp.vipo.seller.common.dao.entity.FinancialReportEntity;
import com.vtp.vipo.seller.common.dao.entity.RevenueReportEntity;
import com.vtp.vipo.seller.common.dao.entity.TopProductReportEntity;
import com.vtp.vipo.seller.common.dto.response.financial.FinancialReportResponse;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FinancialReportConverter {

    /**
     * Converts a RevenueReportEntity along with its associated child data into a FinancialReportResponse.
     * <p>
     * This method maps the provided lists of TopProductReportEntity, ChartTopProductReportEntity, and
     * FinancialReportEntity into their corresponding DTO representations, then constructs and returns a
     * FinancialReportResponse containing all the mapped data as well as the provided TotalVisitsData.
     * </p>
     *
     * @param report                 The revenue report entity.
     * @param topProductReports      A list of top product report entities.
     * @param chartTopProductReports A list of chart top product report entities.
     * @param financialReports       A list of financial report entities.
     * @param totalVisitsData        The total visits data.
     * @return A FinancialReportResponse object combining all provided data.
     */
    public static FinancialReportResponse convertToResponse(
            RevenueReportEntity report,
            List<TopProductReportEntity> topProductReports,
            List<ChartTopProductReportEntity> chartTopProductReports,
            List<FinancialReportEntity> financialReports,
            FinancialReportResponse.TotalVisitsData totalVisitsData
    ) {
        // Map the list of TopProductReportEntity to a list of TopProduct DTOs.
        List<FinancialReportResponse.TopProduct> topProducts = topProductReports.stream()
                .map(FinancialReportConverter::mapTopProductReportEntity)
                .collect(Collectors.toList());

        // Map the list of ChartTopProductReportEntity to ChartData containing a list of ChartDataItem DTOs.
        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (!chartTopProductReports.isEmpty()) {
            totalRevenue = chartTopProductReports.get(0).getTotalRevenue();
        }
        FinancialReportResponse.ChartData chartData = FinancialReportResponse.ChartData.builder()
                .totalRevenue(totalRevenue)
                .data(chartTopProductReports.stream()
                        .map(FinancialReportConverter::mapChartTopProductReportEntity)
                        .collect(Collectors.toList()))
                .build();

        // Sort the financial report entities by display order and map them to FinancialDataItem DTOs.
        List<FinancialReportResponse.FinancialDataItem> financialDataItems = financialReports.stream()
                .sorted(Comparator.comparing(FinancialReportEntity::getDisplayOrder))
                .map(FinancialReportConverter::mapFinancialReportEntity)
                .collect(Collectors.toList());

        // Build and return the FinancialReportResponse with all mapped data.
        return FinancialReportResponse.builder()
                .revenueReportId(String.valueOf(report.getId()))
                .period(report.getPeriodType())
                .periodStart(report.getPeriodStart())
                .periodEnd(report.getPeriodEnd())
                .totalOrderPackages(report.getTotalOrderPackages())
                .totalBuyers(report.getTotalBuyers())
                .conversionRate(report.getConversionRate())
                .deliveryRate(report.getDeliveryRate())
                .returnCancelRate(report.getReturnCancelRate())
                .revenue(report.getRevenue())
                .totalPriceNegotiated(report.getTotalPriceNegotiated())
                .totalProfit(report.getTotalProfit())
                .platformFee(report.getTotalPlatformFee())
                .otherFee(report.getTotalOtherFees())
                .topProductReport(topProducts)
                .chartData(chartData)
                .financialData(financialDataItems)
                .totalVisitsData(totalVisitsData)
                .build();
    }

    /**
     * Maps a TopProductReportEntity to its corresponding TopProduct DTO.
     * <p>
     * This method extracts and maps all relevant fields from the given TopProductReportEntity,
     * including mapping its list of SKU items into TopProductItem DTOs.
     * </p>
     *
     * @param entity The TopProductReportEntity to map.
     * @return A TopProduct DTO containing the mapped data.
     */
    private static FinancialReportResponse.TopProduct mapTopProductReportEntity(TopProductReportEntity entity) {
        return FinancialReportResponse.TopProduct.builder()
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .productCode(entity.getProductCode())
                .quantitySold(entity.getQuantitySold())
                .revenueBeforeNegotiation(entity.getRevenueBeforeNegotiation())
                .revenueAfterNegotiation(entity.getRevenueAfterNegotiation())
                .revenuePercentage(entity.getRevenuePercentage())
                .ranking(entity.getRanking())
                // Sort SKUs by ranking and map them to TopProductItem DTOs.
                .skus(entity.getSkus().stream()
                        .sorted(Comparator.comparing(TopProductReportEntity.TopProductItem::getRanking))
                        .map(FinancialReportConverter::mapTopProductItemEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Maps a TopProductItem entity to its corresponding TopProductItem DTO.
     * <p>
     * This method maps the fields of the provided TopProductItem entity to create a TopProductItem DTO.
     * </p>
     *
     * @param entity The TopProductItem entity to map.
     * @return A TopProductItem DTO containing the mapped data.
     */
    private static FinancialReportResponse.TopProductItem mapTopProductItemEntity(TopProductReportEntity.TopProductItem entity) {
        return FinancialReportResponse.TopProductItem.builder()
                .skuId(entity.getSkuId())
                .label(entity.getLabel())
                .quantitySold(entity.getQuantitySold())
                .revenueBeforeNegotiation(entity.getRevenueBeforeNegotiation())
                .revenueAfterNegotiation(entity.getRevenueAfterNegotiation())
                .ranking(entity.getRanking())
                .build();
    }

    /**
     * Maps a ChartTopProductReportEntity to its corresponding ChartDataItem DTO.
     * <p>
     * This method extracts the label, percentage, and ranking from the provided entity and builds a ChartDataItem DTO.
     * </p>
     *
     * @param entity The ChartTopProductReportEntity to map.
     * @return A ChartDataItem DTO containing the mapped data.
     */
    private static FinancialReportResponse.ChartDataItem mapChartTopProductReportEntity(ChartTopProductReportEntity entity) {
        return FinancialReportResponse.ChartDataItem.builder()
                .label(entity.getLabel())
                .percentage(entity.getPercentage())
                .ranking(entity.getRanking())
                .build();
    }

    /**
     * Maps a FinancialReportEntity to its corresponding FinancialDataItem DTO.
     * <p>
     * This method extracts the period name and financial metrics (total revenue, total cost, total profit)
     * from the provided FinancialReportEntity and builds a FinancialDataItem DTO.
     * </p>
     *
     * @param entity The FinancialReportEntity to map.
     * @return A FinancialDataItem DTO containing the mapped data.
     */
    private static FinancialReportResponse.FinancialDataItem mapFinancialReportEntity(FinancialReportEntity entity) {
        return FinancialReportResponse.FinancialDataItem.builder()
                .periodName(entity.getPeriodName())
                .totalRevenue(entity.getTotalRevenue())
                .totalCost(entity.getTotalCost())
                .totalProfit(entity.getTotalProfit())
                .build();
    }
}

