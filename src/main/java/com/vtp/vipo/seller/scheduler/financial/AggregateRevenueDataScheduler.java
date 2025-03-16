package com.vtp.vipo.seller.scheduler.financial;

import com.vtp.vipo.seller.common.dao.entity.RevenueReportEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.PeriodType;
import com.vtp.vipo.seller.common.dao.repository.MerchantRepository;
import com.vtp.vipo.seller.common.dto.request.financial.FinancialReportRequest;
import com.vtp.vipo.seller.common.dto.response.financial.FinancialReportResponse;
import com.vtp.vipo.seller.common.dto.response.financial.TimeRange;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.services.financial.AggregateRevenueService;
import com.vtp.vipo.seller.services.financial.PeriodCalculator;
import com.vtp.vipo.seller.services.financial.RevenueReportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@DisallowConcurrentExecution
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AggregateRevenueDataScheduler extends QuartzJobBean {

    AggregateRevenueService aggregateRevenueService;

    RevenueReportService revenueReportService;

    MerchantRepository merchantRepository;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("[AggregateRevenueDataScheduler] Starting revenue aggregation");

        // Retrieve all merchant IDs
        List<Long> merchantIds = merchantRepository.getMerchantIds();
        log.info("[AggregateRevenueDataScheduler] Retrieved {} merchants", merchantIds.size());

        if (merchantIds.isEmpty()) {
            log.info("[AggregateRevenueDataScheduler] No merchants found. Exiting revenue aggregation.");
            return;
        }

        // Aggregate revenue data for various period types
        aggregateRevenueData(merchantIds, PeriodType.DAY);
        aggregateRevenueData(merchantIds, PeriodType.WEEK);
        aggregateRevenueData(merchantIds, PeriodType.MONTH);
        aggregateRevenueData(merchantIds, PeriodType.QUARTER);
        aggregateRevenueData(merchantIds, PeriodType.YEAR);
    }

    /**
     * Generic method for aggregating revenue data for each merchant based on the specified period type.
     *
     * @param merchantIds List of merchant IDs
     * @param periodType  The period type for which the revenue data is to be aggregated (e.g., DAY, WEEK, MONTH, QUARTER, YEAR)
     */
    private void aggregateRevenueData(List<Long> merchantIds, PeriodType periodType) {
        // Build the financial report request based on the period type and current date minus one day.
        FinancialReportRequest request = PeriodCalculator.buildFinancialReportRequest(periodType, LocalDate.now().minusDays(1));
        log.info("[AggregateRevenueDataScheduler] Build financial report request for {}: {}", periodType, request);
        if (ObjectUtils.isEmpty(request)) {
            log.error("[AggregateRevenueDataScheduler] Failed to build financial report request for period type: {}", periodType);
            return;
        }

        // Calculate the time range based on the request.
        TimeRange timeRange = PeriodCalculator.calculateTimeRange(request);
        log.info("[AggregateRevenueDataScheduler] Calculated time range for {}: {}", periodType, timeRange);
        if (ObjectUtils.isEmpty(timeRange)) {
            log.error("[AggregateRevenueDataScheduler] Failed to calculate time range for period type: {}", periodType);
            return;
        }

        // For each merchant, aggregate the revenue data and either insert or update the report.
        merchantIds.forEach(merchantId -> {
            try {
                log.info("[AggregateRevenueDataScheduler] Aggregating revenue data for merchantId: {} with period {} and timeRange: {}",
                        merchantId, periodType, timeRange);

                // Aggregate revenue data.
                FinancialReportResponse response = aggregateRevenueService.aggregateRevenueData(request, merchantId, timeRange);
                log.info("[AggregateRevenueDataScheduler] response: {}", response);
                if (ObjectUtils.isEmpty(response)) {
                    log.error("[AggregateRevenueDataScheduler] Failed to aggregate revenue data for merchantId: {} with period {} and timeRange: {}",
                            merchantId, periodType, timeRange);
                    return;
                }

                // Determine start and end dates from the timeRange for the lookup.
                LocalDate startDate = DateUtils.getLocalDateTimeFromEpochSecond(timeRange.getCurrentStartSec()).toLocalDate();
                LocalDate endDate = DateUtils.getLocalDateTimeFromEpochSecond(timeRange.getCurrentEndSec()).toLocalDate();

                // Check if a report already exists in the database for the given merchant and period.
                Optional<RevenueReportEntity> existingReportOpt = revenueReportService.findReportByPeriod(merchantId, periodType, startDate, endDate);
                if (existingReportOpt.isPresent()) {
                    log.info("[AggregateRevenueDataScheduler] Report exists for merchantId: {} with period {} and timeRange: {}. Updating report.",
                            merchantId, periodType, timeRange);
                    // For an update, set the report id in the response (if required) and then update.
                    RevenueReportEntity existingReport = existingReportOpt.get();
                    response.setRevenueReportId(String.valueOf(existingReport.getId()));
                    revenueReportService.update(response, existingReport.getId(), timeRange);
                    log.info("[AggregateRevenueDataScheduler] Updated revenue report with reportId: {}", existingReport.getId());
                } else {
                    log.info("[AggregateRevenueDataScheduler] No existing report for merchantId: {} with period {} and timeRange: {}. Inserting new record.",
                            merchantId, periodType, timeRange);
                    // For insert, simply save the new report.
                    Long reportId = revenueReportService.save(response, merchantId, timeRange);
                    if (ObjectUtils.isNotEmpty(reportId)) {
                        log.info("[AggregateRevenueDataScheduler] Inserted new revenue report with reportId: {}", reportId);
                    } else {
                        log.error("[AggregateRevenueDataScheduler] Failed to insert revenue report for merchantId: {} with period {} and timeRange: {}",
                                merchantId, periodType, timeRange);
                    }
                }
            } catch (Exception e) {
                log.error("[AggregateRevenueDataScheduler] Error while aggregating revenue data for merchantId: {} with period {} and timeRange: {}",
                        merchantId, periodType, timeRange, e);
            }
        });
    }
}


