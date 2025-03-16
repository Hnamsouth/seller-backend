package com.vtp.vipo.seller.services.financial;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.PeriodType;
import com.vtp.vipo.seller.common.dto.request.financial.FinancialReportRequest;
import com.vtp.vipo.seller.common.dto.response.financial.TimeRange;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Slf4j
public class PeriodCalculator {

    /**
     * Creates a FinancialReportRequest based on the given PeriodType and reference date.
     * <p>
     * This method constructs a FinancialReportRequest object by setting its filter type and filter value
     * according to the provided PeriodType and reference date. The filter value is formatted as follows:
     * <ul>
     *   <li>DAY: "yyyy-MM-dd" (e.g., "2024-12-02")</li>
     *   <li>WEEK: "yyyy-Wxx" (e.g., "2024-W10", where xx is the week number)</li>
     *   <li>MONTH: "yyyy-MM" (e.g., "2024-12")</li>
     *   <li>QUARTER: "yyyy-Qx" (e.g., "2024-Q4", where x is the quarter number)</li>
     *   <li>YEAR: "yyyy" (e.g., "2024")</li>
     * </ul>
     * If the provided period type is invalid, a VipoBusinessException is thrown.
     * </p>
     *
     * @param periodType    The period type (DAY, WEEK, MONTH, QUARTER, YEAR).
     * @param referenceDate The reference date from which the filter value is derived.
     * @return A FinancialReportRequest with the appropriate filterType and filterValue.
     * @throws VipoBusinessException if the period type is invalid.
     */
    public static FinancialReportRequest buildFinancialReportRequest(PeriodType periodType, LocalDate referenceDate) {
        log.info("Building financial report request for period type {} and reference date {}", periodType, referenceDate);
        FinancialReportRequest request = new FinancialReportRequest();
        // Convert periodType to a lowercase string (e.g., "day", "week", etc.)
        String filterType = periodType.name().toLowerCase();
        String filterValue = "";

        // Build the filter value based on the period type.
        switch (periodType) {
            case DAY:
                // For DAY, format the reference date as "yyyy-MM-dd"
                filterValue = referenceDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                break;
            case WEEK:
                // Sử dụng ISO week fields để đảm bảo tuần bắt đầu từ thứ 2
                WeekFields weekFields = WeekFields.ISO;
                int weekNumber = referenceDate.get(weekFields.weekOfWeekBasedYear());
                int weekBasedYear = referenceDate.get(weekFields.weekBasedYear());
                // Format as "yyyy-Wxx" with two-digit week number.
                filterValue = String.format("%d-W%02d", weekBasedYear, weekNumber);
                break;
            case MONTH:
                // For MONTH, format the reference date as "yyyy-MM"
                filterValue = referenceDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                break;
            case QUARTER:
                // For QUARTER, determine the quarter number from the month.
                int quarter = (referenceDate.getMonthValue() - 1) / 3 + 1;
                filterValue = String.format("%d-Q%d", referenceDate.getYear(), quarter);
                break;
            case YEAR:
                // For YEAR, simply use the year.
                filterValue = String.valueOf(referenceDate.getYear());
                break;
            default:
                throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                        "Kỳ tổng hợp không hợp lệ");
        }

        request.setFilterType(filterType);
        request.setFilterValue(filterValue);
        // Note: productIds remain empty in Phase6; will be used in a later phase.
        return request;
    }

    /**
     * Calculates the TimeRange based on the provided FinancialReportRequest.
     * <p>
     * This method determines the start and end times (in epoch seconds) for the current and previous periods
     * based on the filter type and filter value in the request. The rules are as follows:
     * <ul>
     *   <li><b>day</b>: The current period is the specified day (from 00:00:00 to 23:59:59),
     *       and the previous period is the day immediately before.</li>
     *   <li><b>week</b>: The current period is the week (Monday to Sunday) corresponding to the filter value,
     *       and the previous period is the week immediately preceding the current week.</li>
     *   <li><b>month</b>: The current period covers the entire month,
     *       and the previous period is the month immediately before.</li>
     *   <li><b>quarter</b>: The current period spans the three months of the quarter,
     *       and the previous period is the quarter immediately before.</li>
     *   <li><b>year</b>: The current period covers the entire year,
     *       and the previous period is the year immediately preceding it.</li>
     * </ul>
     * After computing the LocalDateTime values for each period, they are converted to epoch seconds using the system default time zone.
     * Additionally, if the computed end time of the current period exceeds the effective current time (i.e. today minus one day),
     * it is adjusted accordingly.
     * </p>
     *
     * @param request The financial report request containing filterType and filterValue.
     * @return A TimeRange object with epoch seconds for currentStartSec, currentEndSec, previousStartSec, and previousEndSec.
     * @throws VipoBusinessException if the filter type is invalid.
     */
    public static TimeRange calculateTimeRange(FinancialReportRequest request) {
        String filterType = request.getFilterType().toLowerCase();
        String filterValue = request.getFilterValue();

        // Variables to hold start and end times for the current period.
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        // Variables for the previous period.
        LocalDateTime prevStartDateTime;
        LocalDateTime prevEndDateTime;

        switch (filterType) {
            case "day": {
                // Parse the day from the filter value (format "yyyy-MM-dd").
                LocalDate day = LocalDate.parse(filterValue, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                startDateTime = day.atStartOfDay();
                // Set the end time to the last second of the day.
                endDateTime = day.plusDays(1).atStartOfDay().minusSeconds(1);

                // Previous period is the day immediately before.
                LocalDate prevDay = day.minusDays(1);
                prevStartDateTime = prevDay.atStartOfDay();
                prevEndDateTime = prevDay.plusDays(1).atStartOfDay().minusSeconds(1);
                break;
            }
            case "week": {
                // Split filter value "yyyy-Wxx" into year and week number.
                String[] parts = filterValue.split("-W");
                int year = Integer.parseInt(parts[0]);
                int week = Integer.parseInt(parts[1]);

                // Calculate the start of the week (Monday) for the given week.
                LocalDate startOfWeek = LocalDate.of(year, 1, 1)
                        .with(WeekFields.ISO.weekOfYear(), week)
                        .with(WeekFields.ISO.dayOfWeek(), 1);
                // End of week is 6 days after the start.
                LocalDate endOfWeek = startOfWeek.plusDays(6);

                startDateTime = startOfWeek.atStartOfDay();
                endDateTime = endOfWeek.plusDays(1).atStartOfDay().minusSeconds(1);

                // Previous period is the week immediately preceding the current week.
                LocalDate prevStartOfWeek = startOfWeek.minusWeeks(1);
                LocalDate prevEndOfWeek = prevStartOfWeek.plusDays(6);

                prevStartDateTime = prevStartOfWeek.atStartOfDay();
                prevEndDateTime = prevEndOfWeek.plusDays(1).atStartOfDay().minusSeconds(1);
                break;
            }
            case "month": {
                // Parse the month from the filter value (format "yyyy-MM").
                YearMonth ym = YearMonth.parse(filterValue, DateTimeFormatter.ofPattern("yyyy-MM"));
                LocalDate startOfMonth = ym.atDay(1);
                LocalDate endOfMonth = ym.atEndOfMonth();

                startDateTime = startOfMonth.atStartOfDay();
                endDateTime = endOfMonth.plusDays(1).atStartOfDay().minusSeconds(1);

                // Previous period is the month immediately before.
                YearMonth prevYm = ym.minusMonths(1);
                LocalDate prevStartOfMonth = prevYm.atDay(1);
                LocalDate prevEndOfMonth = prevYm.atEndOfMonth();

                prevStartDateTime = prevStartOfMonth.atStartOfDay();
                prevEndDateTime = prevEndOfMonth.plusDays(1).atStartOfDay().minusSeconds(1);
                break;
            }
            case "quarter": {
                // Parse the quarter from the filter value (format "yyyy-Qx").
                String[] arr = filterValue.split("-Q");
                int year = Integer.parseInt(arr[0]);
                int q = Integer.parseInt(arr[1]);

                // Determine the starting month of the quarter.
                int startMonth = (q - 1) * 3 + 1;
                YearMonth startYM = YearMonth.of(year, startMonth);
                // The quarter spans 3 months.
                YearMonth endYM = startYM.plusMonths(2);

                LocalDate startDate = startYM.atDay(1);
                LocalDate endDate = endYM.atEndOfMonth();

                startDateTime = startDate.atStartOfDay();
                endDateTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1);

                // Previous quarter is the quarter immediately before (subtract 3 months).
                YearMonth prevStartYM = startYM.minusMonths(3);
                YearMonth prevEndYM = prevStartYM.plusMonths(2);

                LocalDate prevStartDate = prevStartYM.atDay(1);
                LocalDate prevEndDate = prevEndYM.atEndOfMonth();

                prevStartDateTime = prevStartDate.atStartOfDay();
                prevEndDateTime = prevEndDate.plusDays(1).atStartOfDay().minusSeconds(1);
                break;
            }
            case "year": {
                // Parse the year from the filter value.
                int year = Integer.parseInt(filterValue);
                LocalDate startDate = LocalDate.of(year, 1, 1);
                LocalDate endDate = LocalDate.of(year, 12, 31);

                startDateTime = startDate.atStartOfDay();
                endDateTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1);

                // Previous period is the year immediately before.
                int prevYear = year - 1;
                LocalDate prevStart = LocalDate.of(prevYear, 1, 1);
                LocalDate prevEnd = LocalDate.of(prevYear, 12, 31);

                prevStartDateTime = prevStart.atStartOfDay();
                prevEndDateTime = prevEnd.plusDays(1).atStartOfDay().minusSeconds(1);
                break;
            }
            default:
                throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                        "Lọc theo không hợp lệ");
        }

        // Convert LocalDateTime values to epoch seconds using the system default time zone.
        ZoneId zone = ZoneId.systemDefault();

        // Adjust the end time if it exceeds the effective current time (i.e. today minus 1 day).
        if (endDateTime.isAfter(LocalDateTime.now().minusDays(1))) {
            endDateTime = LocalDateTime.now().toLocalDate().atStartOfDay().minusSeconds(1);
        }

        long currentStartSec = startDateTime.atZone(zone).toEpochSecond();
        long currentEndSec = endDateTime.atZone(zone).toEpochSecond();
        long previousStartSec = prevStartDateTime.atZone(zone).toEpochSecond();
        long previousEndSec = prevEndDateTime.atZone(zone).toEpochSecond();

        // Build and return the TimeRange object.
        return TimeRange.builder()
                .currentStartSec(currentStartSec)
                .currentEndSec(currentEndSec)
                .previousStartSec(previousStartSec)
                .previousEndSec(previousEndSec)
                .build();
    }
}
