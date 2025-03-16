package com.vtp.vipo.seller.services.financial.impl;

import com.vtp.vipo.seller.common.dao.entity.ChartTopProductReportEntity;
import com.vtp.vipo.seller.common.dao.entity.FinancialReportEntity;
import com.vtp.vipo.seller.common.dao.entity.RevenueReportEntity;
import com.vtp.vipo.seller.common.dao.entity.TopProductReportEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.PeriodType;
import com.vtp.vipo.seller.common.dao.repository.ChartTopProductReportEntityRepository;
import com.vtp.vipo.seller.common.dao.repository.FinancialReportEntityRepository;
import com.vtp.vipo.seller.common.dao.repository.RevenueReportEntityRepository;
import com.vtp.vipo.seller.common.dao.repository.TopProductReportEntityRepository;
import com.vtp.vipo.seller.common.dto.response.financial.FinancialReportResponse;
import com.vtp.vipo.seller.common.dto.response.financial.TimeRange;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.services.financial.FinancialUtils;
import com.vtp.vipo.seller.services.financial.RevenueReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueReportServiceImpl implements RevenueReportService {

    final RevenueReportEntityRepository revenueReportEntityRepository;

    final TopProductReportEntityRepository topProductReportEntityRepository;

    final ChartTopProductReportEntityRepository chartTopProductReportEntityRepository;

    final FinancialReportEntityRepository financialReportEntityRepository;

    /**
     * Save a new revenue report along with its child records.
     * <p>
     * This method is used to insert a new revenue report into the database. It delegates the
     * actual saving logic to a unified method {@link #saveOrUpdate(FinancialReportResponse, Long, TimeRange, boolean)}
     * with the update flag set to {@code false}.
     * </p>
     *
     * @param reportData The financial report data to be saved.
     * @param merchantId The merchant's identifier.
     * @param timeRange  The time range for the report.
     * @return The generated report ID after successful save.
     */
    @Transactional
    @Override
    public Long save(FinancialReportResponse reportData, Long merchantId, TimeRange timeRange) {
        // For new records, call the unified method with isUpdate = false.
        return saveOrUpdate(reportData, merchantId, timeRange, false);
    }

    /**
     * Update an existing revenue report along with its child records.
     * <p>
     * This method updates an existing revenue report in the database. It delegates the actual
     * update logic to a unified method {@link #saveOrUpdate(FinancialReportResponse, Long, TimeRange, boolean)}
     * with the update flag set to {@code true}. The merchantId is not passed explicitly (i.e., set to null)
     * so that the method uses the merchantId from the existing record.
     * </p>
     *
     * @param reportData The updated financial report data.
     * @param reportId   The identifier of the existing report to update.
     * @param timeRange  The time range for the report.
     */
    @Transactional
    @Override
    public void update(FinancialReportResponse reportData, Long reportId, TimeRange timeRange) {
        // For update, pass null as merchantId to let the unified method use the existing record's merchantId.
        saveOrUpdate(reportData, null, timeRange, true);
    }

    /**
     * Find a revenue report by period.
     * <p>
     * This method searches for an existing revenue report for a specified merchant within a defined period.
     * The period is determined by the given {@code PeriodType} and the specified start and end dates.
     * </p>
     *
     * @param merchantId The merchant's identifier.
     * @param periodType The type of period (e.g., DAY, WEEK, MONTH, QUARTER, YEAR).
     * @param startDate  The start date of the period.
     * @param endDate    The end date of the period.
     * @return An {@code Optional} containing the found {@link RevenueReportEntity} if it exists, or empty otherwise.
     */
    @Override
    public Optional<RevenueReportEntity> findReportByPeriod(Long merchantId, PeriodType periodType, LocalDate startDate, LocalDate endDate) {
        return revenueReportEntityRepository.findReportByPeriod(merchantId, periodType, startDate, endDate);
    }

    /**
     * Unified method that handles both insert and update.
     *
     * @param reportData The financial report response containing data for the revenue report.
     * @param merchantId The merchant id. For update operations, if null, the existing record's merchantId is used.
     * @param timeRange  The time range for the report.
     * @param isUpdate   Flag indicating whether to update (true) or insert (false).
     * @return The id of the revenue report record.
     */
    private Long saveOrUpdate(FinancialReportResponse reportData, Long merchantId, TimeRange timeRange, boolean isUpdate) {
        // Convert epoch seconds from the timeRange to LocalDate for periodStart and periodEnd.
        LocalDate periodStart = DateUtils.getLocalDateTimeFromEpochSecond(timeRange.getCurrentStartSec()).toLocalDate();
        LocalDate periodEnd = DateUtils.getLocalDateTimeFromEpochSecond(timeRange.getCurrentEndSec()).toLocalDate();
        Long reportId = null;

        if (!isUpdate) {
            // ----- INSERT LOGIC -----
            // Insert a new revenue report record
            reportId = saveReport(reportData, merchantId, periodStart, periodEnd);
            log.info("[saveOrUpdate] Inserted new revenue report id={}", reportId);
        } else {
            // ----- UPDATE LOGIC -----
            // For update, the reportData must contain the revenueReportId
            if (ObjectUtils.isEmpty(reportData.getRevenueReportId())) {
                log.error("[saveOrUpdate] Report id is missing for update.");
                return null;
            }

            // Retrieve the existing report from the database
            reportId = Long.valueOf(reportData.getRevenueReportId());
            Optional<RevenueReportEntity> opt = revenueReportEntityRepository.findById(reportId);
            if (opt.isEmpty()) {
                log.error("[saveOrUpdate] No existing report found with id={}", reportId);
                return null;
            }

            // If merchantId is not provided for update, use the merchantId from the existing record.
            RevenueReportEntity entity = opt.get();
            if (ObjectUtils.isEmpty(merchantId)) {
                merchantId = entity.getMerchantId();
            }

            // Update the fields of the existing report entity with the new data.
            entity.setPeriodStart(periodStart);
            entity.setPeriodEnd(periodEnd);
            entity.setTotalOrderPackages(FinancialUtils.safeMetric(reportData.getTotalOrderPackages()));
            entity.setTotalBuyers(FinancialUtils.safeMetric(reportData.getTotalBuyers()));
            entity.setConversionRate(FinancialUtils.safe(reportData.getConversionRate()));
            entity.setDeliveryRate(FinancialUtils.safe(reportData.getDeliveryRate()));
            entity.setReturnCancelRate(FinancialUtils.safe(reportData.getReturnCancelRate()));
            entity.setRevenue(FinancialUtils.safeMetric(reportData.getRevenue()));
            entity.setTotalPriceNegotiated(FinancialUtils.safe(reportData.getTotalPriceNegotiated()));
            entity.setTotalProfit(FinancialUtils.safeMetric(reportData.getTotalProfit()));
            entity.setTotalPlatformFee(FinancialUtils.safe(reportData.getPlatformFee()));
            entity.setTotalOtherFees(FinancialUtils.safe(reportData.getOtherFee()));
            entity.setMerchantId(merchantId);

            // Save the updated entity to the database.
            RevenueReportEntity updatedEntity = revenueReportEntityRepository.save(entity);
            reportId = updatedEntity.getId();
            log.info("[saveOrUpdate] Updated revenue report id={}", reportId);
        }

        if (ObjectUtils.isEmpty(reportId)) {
            log.error("[saveOrUpdate] Failed to save or update revenue report");
            return null;
        }

        // ----- CHILD RECORDS -----
        // For update operations, remove the old child records so that new ones can be inserted.
        if (isUpdate) {
            topProductReportEntityRepository.deleteByReportId(reportId);
            chartTopProductReportEntityRepository.deleteByReportId(reportId);
            financialReportEntityRepository.deleteByReportId(reportId);
            log.info("[saveOrUpdate] Cleared existing child records for report id={}", reportId);
        }

        // Save the child records regardless of whether we are inserting or updating.
        saveTopProducts(reportData, reportId);
        saveChartTopProducts(reportData, reportId);
        saveFinancialReport(reportData, reportId);

        return reportId;
    }

    /**
     * Inserts a new revenue report record into the database.
     * <p>
     * This method builds a {@link RevenueReportEntity} based on the provided
     * financial report data, merchant identifier, and period dates. It uses safe conversions
     * (via {@code FinancialUtils.safe} and {@code FinancialUtils.safeMetric}) for numeric fields.
     * The constructed entity is then saved using the repository and its generated ID is returned.
     * </p>
     *
     * @param report      The financial report data to save.
     * @param merchantId  The merchant's identifier.
     * @param periodStart The starting date of the reporting period.
     * @param periodEnd   The ending date of the reporting period.
     * @return The generated ID of the saved revenue report.
     */
    private Long saveReport(FinancialReportResponse report, Long merchantId, LocalDate periodStart, LocalDate periodEnd) {
        // Build the revenue report entity using the provided data.
        RevenueReportEntity entity = RevenueReportEntity.builder()
                // Set the period dates.
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                // Set the period type from the report.
                .periodType(report.getPeriod())
                // Set productIds if needed (currently null).
                .productIds(null)
                // Set metric values using safe conversion methods.
                .totalOrderPackages(FinancialUtils.safeMetric(report.getTotalOrderPackages()))
                .totalBuyers(FinancialUtils.safeMetric(report.getTotalBuyers()))
                .conversionRate(FinancialUtils.safe(report.getConversionRate()))
                .deliveryRate(FinancialUtils.safe(report.getDeliveryRate()))
                .returnCancelRate(FinancialUtils.safe(report.getReturnCancelRate()))
                .revenue(FinancialUtils.safeMetric(report.getRevenue()))
                .totalPriceNegotiated(FinancialUtils.safe(report.getTotalPriceNegotiated()))
                .totalProfit(FinancialUtils.safeMetric(report.getTotalProfit()))
                .totalPlatformFee(FinancialUtils.safe(report.getPlatformFee()))
                .totalOtherFees(FinancialUtils.safe(report.getOtherFee()))
                // Set the merchant identifier.
                .merchantId(merchantId)
                .build();

        // Save the entity and log the generated ID.
        RevenueReportEntity savedEntity = revenueReportEntityRepository.save(entity);
        log.info("[saveReport] Saved revenue report id={}", savedEntity.getId());
        return savedEntity.getId();
    }

    /**
     * Saves top product report records associated with a revenue report.
     * <p>
     * This method iterates over the list of top product reports contained in the provided
     * {@code FinancialReportResponse} object. For each product, it maps the data to a
     * {@link TopProductReportEntity} and accumulates them into a list, which is then saved in batch.
     * If the top product report list is empty, the method returns without saving any records.
     * </p>
     *
     * @param report   The financial report data containing top product reports.
     * @param reportId The identifier of the associated revenue report.
     */
    private void saveTopProducts(FinancialReportResponse report, Long reportId) {
        // Check if the top product report list is empty; if so, exit.
        if (ObjectUtils.isEmpty(report.getTopProductReport())) {
            return;
        }

        List<TopProductReportEntity> topProductReportEntities = new ArrayList<>();
        // Map each product from the report to a TopProductReportEntity.
        report.getTopProductReport().forEach(product -> {
            TopProductReportEntity entity = TopProductReportEntity.builder()
                    .reportId(reportId)
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .productCode(product.getProductCode())
                    .quantitySold(product.getQuantitySold())
                    .revenueBeforeNegotiation(product.getRevenueBeforeNegotiation())
                    .revenueAfterNegotiation(product.getRevenueAfterNegotiation())
                    .revenuePercentage(product.getRevenuePercentage())
                    .ranking(product.getRanking())
                    // Map each SKU in the product.
                    .skus(product.getSkus().stream().map(sku -> TopProductReportEntity.TopProductItem.builder()
                            .skuId(sku.getSkuId())
                            .label(sku.getLabel())
                            .quantitySold(sku.getQuantitySold())
                            .revenueBeforeNegotiation(sku.getRevenueBeforeNegotiation())
                            .revenueAfterNegotiation(sku.getRevenueAfterNegotiation())
                            .ranking(sku.getRanking())
                            .build()).toList())
                    .build();
            topProductReportEntities.add(entity);
        });

        // Save all top product records in batch.
        topProductReportEntityRepository.saveAll(topProductReportEntities);
        log.info("[saveTopProducts] Saved top products for report id={}", reportId);
    }

    /**
     * Saves chart top product report records associated with a revenue report.
     * <p>
     * This method retrieves the chart data from the given {@code FinancialReportResponse} and
     * maps each data point to a {@link ChartTopProductReportEntity}. If the chart data or its data
     * list is empty, the method returns without saving any records.
     * </p>
     *
     * @param report   The financial report data containing chart data.
     * @param reportId The identifier of the associated revenue report.
     */
    private void saveChartTopProducts(FinancialReportResponse report, Long reportId) {
        // Check if chart data or its list is empty; if so, exit.
        if (ObjectUtils.isEmpty(report.getChartData()) || ObjectUtils.isEmpty(report.getChartData().getData())) {
            return;
        }

        List<ChartTopProductReportEntity> chartTopProductReportEntities = new ArrayList<>();
        // Map each chart data point to a ChartTopProductReportEntity.
        report.getChartData().getData().forEach(chartData -> {
            ChartTopProductReportEntity entity = ChartTopProductReportEntity.builder()
                    .reportId(reportId)
                    .label(chartData.getLabel())
                    .percentage(chartData.getPercentage())
                    .ranking(chartData.getRanking())
                    .displayOrder(chartData.getDisplayOrder())
                    .totalRevenue(report.getChartData().getTotalRevenue())
                    .build();
            chartTopProductReportEntities.add(entity);
        });

        // Save all chart top product records in batch.
        chartTopProductReportEntityRepository.saveAll(chartTopProductReportEntities);
        log.info("[saveChartTopProducts] Saved chart top products for report id={}", reportId);
    }

    /**
     * Saves financial report records associated with a revenue report.
     * <p>
     * This method iterates over the financial data items contained in the provided
     * {@code FinancialReportResponse}. For each item, it maps the data to a {@link FinancialReportEntity}
     * and accumulates the entities into a list. The list is then saved in batch. If no financial data is
     * present in the report, the method returns without performing any save.
     * </p>
     *
     * @param report   The financial report data containing financial data items.
     * @param reportId The identifier of the associated revenue report.
     */
    private void saveFinancialReport(FinancialReportResponse report, Long reportId) {
        // Check if financial data is empty; if so, exit.
        if (ObjectUtils.isEmpty(report.getFinancialData())) {
            return;
        }

        List<FinancialReportEntity> financialReportEntities = new ArrayList<>();
        // Map each financial data item to a FinancialReportEntity.
        report.getFinancialData().forEach(financialData -> {
            FinancialReportEntity entity = FinancialReportEntity.builder()
                    .reportId(reportId)
                    .periodName(financialData.getPeriodName())
                    .totalRevenue(financialData.getTotalRevenue())
                    .totalCost(financialData.getTotalCost())
                    .totalProfit(financialData.getTotalProfit())
                    .displayOrder(financialData.getDisplayOrder())
                    .build();
            financialReportEntities.add(entity);
        });

        // Save all financial report records in batch.
        financialReportEntityRepository.saveAll(financialReportEntities);
        log.info("[saveFinancialReport] Saved financial report records for report id={}", reportId);
    }
}

