package com.vtp.vipo.seller.services.financial.impl;

import com.vtp.vipo.seller.common.dao.repository.ProductDailyAnalyticEntityRepository;
import com.vtp.vipo.seller.common.dto.response.financial.FinancialReportResponse;
import com.vtp.vipo.seller.common.dto.response.financial.TotalVisitsProjection;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.services.financial.TotalVisitsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TotalVisitsServiceImpl implements TotalVisitsService {

    final ProductDailyAnalyticEntityRepository productDailyAnalyticEntityRepository;

    /**
     * Retrieves the total visits data for a given merchant within a specified time range.
     * <p>
     * This method converts the given epoch seconds (currStart and currEnd) into LocalDate values,
     * then queries the database for total visits between those dates for the specified merchant.
     * The resulting data is then converted into a {@code FinancialReportResponse.TotalVisitsData} object
     * using the helper method {@link #buildResponse(TotalVisitsProjection)}.
     * </p>
     *
     * @param merchantId The identifier of the merchant.
     * @param currStart  The epoch seconds representing the start of the current period.
     * @param currEnd    The epoch seconds representing the end of the current period.
     * @return A {@code FinancialReportResponse.TotalVisitsData} object containing the total visits
     * and the latest update time.
     */
    @Override
    public FinancialReportResponse.TotalVisitsData getTotalVisits(Long merchantId, long currStart, long currEnd) {
        // Convert epoch seconds to LocalDate for the start and end of the period.
        LocalDate currDateStart = DateUtils.getLocalDateTimeFromEpochSecond(currStart).toLocalDate();
        LocalDate currDateEnd = DateUtils.getLocalDateTimeFromEpochSecond(currEnd).toLocalDate();
        log.info("[Financial getTotalVisits] Get total visits with merchantId: {}, currDateStart: {}, currDateEnd: {}",
                merchantId, currDateStart, currDateEnd);

        // Query the database for total visits data between currDateStart and currDateEnd.
        TotalVisitsProjection totalVisitsData = productDailyAnalyticEntityRepository.getTotalVisits(merchantId, currDateStart, currDateEnd);
        log.info("[Financial getTotalVisits] Total visits: {} - Latest time: {}",
                totalVisitsData.getTotalVisits(), totalVisitsData.getUpdatedAt());

        // Build and return the TotalVisitsData response.
        return buildResponse(totalVisitsData);
    }

    /**
     * Retrieves the total visits data for all time for a given merchant.
     * <p>
     * This method queries the database for total visits data for the specified merchant across all periods.
     * The resulting data is then converted into a {@code FinancialReportResponse.TotalVisitsData} object
     * using the helper method {@link #buildResponse(TotalVisitsProjection)}.
     * </p>
     *
     * @param merchantId The identifier of the merchant.
     * @return A {@code FinancialReportResponse.TotalVisitsData} object containing the total visits
     * and the latest update time.
     */
    @Override
    public FinancialReportResponse.TotalVisitsData getTotalVisitsAllTime(Long merchantId) {
        log.info("[Financial getTotalVisitsAllTime] Get total visits with merchantId: {}", merchantId);

        // Query the database for total visits data for the merchant (all time).
        TotalVisitsProjection totalVisitsData = productDailyAnalyticEntityRepository.getTotalVisitsByMerchantId(merchantId);
        log.info("[Financial getTotalVisitsAllTime] Total visits: {} - Latest time: {}",
                totalVisitsData.getTotalVisits(), totalVisitsData.getUpdatedAt());

        // Build and return the TotalVisitsData response.
        return buildResponse(totalVisitsData);
    }

    /**
     * Builds a {@code FinancialReportResponse.TotalVisitsData} response from the given projection.
     * <p>
     * This helper method checks if the projection and its fields are non-null. If the total visits value
     * or the updated time is missing, it substitutes them with default values (0 for total visits and the current
     * time for the updated time).
     * </p>
     *
     * @param totalVisitsData The projection object containing the total visits data.
     * @return A {@code FinancialReportResponse.TotalVisitsData} object containing the total visits and the
     * update timestamp (in epoch seconds).
     */
    private FinancialReportResponse.TotalVisitsData buildResponse(TotalVisitsProjection totalVisitsData) {
        return FinancialReportResponse.TotalVisitsData.builder()
                .totalVisits(ObjectUtils.isNotEmpty(totalVisitsData)
                        && ObjectUtils.isNotEmpty(totalVisitsData.getTotalVisits())
                        ? totalVisitsData.getTotalVisits()
                        : 0)
                .updatedAt(ObjectUtils.isNotEmpty(totalVisitsData)
                        && ObjectUtils.isNotEmpty(totalVisitsData.getUpdatedAt())
                        ? DateUtils.getTimeInSeconds(totalVisitsData.getUpdatedAt())
                        : DateUtils.getTimeInSeconds(LocalDateTime.now()))
                .build();
    }
}
