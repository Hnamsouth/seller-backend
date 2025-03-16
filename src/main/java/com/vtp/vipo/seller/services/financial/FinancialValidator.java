package com.vtp.vipo.seller.services.financial;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.dto.request.financial.FinancialReportRequest;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Set;
import java.util.regex.Pattern;

public class FinancialValidator {

    // A set of valid filter types.
    private static final Set<String> VALID_FILTER_TYPES = Set.of("day", "week", "month", "quarter", "year");

    // Regex patterns for each filter type:
    // "day" expects a date in the format yyyy-MM-dd.
    private static final Pattern DAY_PATTERN =
            Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$");

    // "week" expects a format of yyyy-Wxx, where xx is the week number from 01 to 53.
    private static final Pattern WEEK_PATTERN =
            Pattern.compile("^\\d{4}-W(0[1-9]|[1-4]\\d|5[0-3])$");

    // "month" expects a format of yyyy-MM.
    private static final Pattern MONTH_PATTERN =
            Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");

    // "quarter" expects a format of yyyy-Qx, where x is between 1 and 4.
    private static final Pattern QUARTER_PATTERN =
            Pattern.compile("^\\d{4}-Q[1-4]$");

    // "year" expects a four-digit year.
    private static final Pattern YEAR_PATTERN =
            Pattern.compile("^\\d{4}$");

    /**
     * Validates the financial report request by ensuring that the filter type and filter value meet the required criteria.
     * <p>
     * This method checks that the filter type is not empty and is one of the allowed values ("day", "week", "month",
     * "quarter", "year"). It also verifies that the filter value is not empty and matches the corresponding regular expression
     * for the specified filter type. Further range validation is performed by calling dedicated methods for each filter type.
     * </p>
     *
     * @param request The financial report request to validate.
     * @throws VipoBusinessException if the filter type is empty, invalid, or if the filter value is empty or improperly formatted.
     */
    public static void validateFinancialReportRequest(FinancialReportRequest request) {
        // Check that the filter type is not empty.
        if (ObjectUtils.isEmpty(request.getFilterType())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    "Lọc theo không được để trống");
        }

        // Check that the filter type is one of the valid types.
        if (!VALID_FILTER_TYPES.contains(request.getFilterType())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    "Lọc theo không hợp lệ: " + request.getFilterType());
        }

        // Check that the filter value is not empty.
        if (ObjectUtils.isEmpty(request.getFilterValue())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    "Thời gian không được để trống");
        }

        // Extract filter type and filter value from the request.
        String filterType = request.getFilterType();
        String filterValue = request.getFilterValue();

        // Validate the filter value based on its type.
        switch (filterType) {
            case "day":
                if (!DAY_PATTERN.matcher(filterValue).matches()) {
                    throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                            "Lọc theo ngày phải có định dạng yyyy-MM-dd, vd: 2025-01-20");
                }
                validateDayRange(filterValue);
                break;

            case "week":
                if (!WEEK_PATTERN.matcher(filterValue).matches()) {
                    throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                            "Lọc theo tuần phải có định dạng yyyy-Wxx, vd: 2025-W03");
                }
                validateWeekRange(filterValue);
                break;

            case "month":
                if (!MONTH_PATTERN.matcher(filterValue).matches()) {
                    throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                            "Lọc theo tháng phải có định dạng yyyy-MM, vd: 2025-01");
                }
                validateMonthRange(filterValue);
                break;

            case "quarter":
                if (!QUARTER_PATTERN.matcher(filterValue).matches()) {
                    throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                            "Lọc theo quý phải có định dạng yyyy-Qx, vd: 2025-Q1");
                }
                validateQuarterRange(filterValue);
                break;

            case "year":
                if (!YEAR_PATTERN.matcher(filterValue).matches()) {
                    throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                            "Lọc theo năm phải có định dạng yyyy, vd: 2025");
                }
                validateYearRange(filterValue);
                break;

            default:
                throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                        "Lọc theo không hợp lệ");
        }
    }

    /**
     * Validates the day range.
     * Allowed period: from (effective today - 6 months) to effective today.
     * <p>
     * Example: If today's date is 2025-02-05, effectiveToday becomes 2025-02-04.
     * Then valid days are from 2024-08-04 (2025-02-04 minus 6 months) to 2025-02-04.
     *
     * @param filterValue The day string to validate.
     */
    private static void validateDayRange(String filterValue) {
        LocalDate parsedDate = LocalDate.parse(filterValue, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate effectiveToday = LocalDate.now().minusDays(1);
        LocalDate maxAllowed = effectiveToday;
        LocalDate minAllowed = effectiveToday.minusMonths(6);

        if (parsedDate.isAfter(maxAllowed)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    String.format("Ngày %s không hợp lệ", filterValue));
        }
        if (parsedDate.isBefore(minAllowed)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    String.format("Ngày %s không hợp lệ. Giới hạn chọn trong vòng 6 tháng", filterValue));
        }
    }

    /**
     * Validates the week range.
     * Allowed period: from (current week start - 1 day - 1 year) to current week start.
     * <p>
     * Example: If today's date is 2025-02-05, effectiveToday becomes 2025-02-04.
     * Then currentWeekStart is computed from effectiveToday (e.g., 2025-02-03), and valid weeks
     * are between (2025-02-03 - 1 day - 1 year) and 2025-02-03.
     *
     * @param filterValue The week string (e.g., "2025-W03") to validate.
     */
    private static void validateWeekRange(String filterValue) {
        String[] parts = filterValue.split("-W");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);

        // Convert the week value to the Monday of that week (ISO standard)
        LocalDate parsedWeekStart = LocalDate.of(year, 1, 1)
                .with(WeekFields.ISO.weekOfYear(), week)
                .with(WeekFields.ISO.dayOfWeek(), 1);
        LocalDate effectiveToday = LocalDate.now().minusDays(1);
        LocalDate currentWeekStart = effectiveToday.with(WeekFields.ISO.dayOfWeek(), 1);
        LocalDate lowerBound = currentWeekStart.minusDays(1).minusYears(1);

        if (parsedWeekStart.isAfter(currentWeekStart)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    String.format("Tuần %s không hợp lệ", filterValue));
        }
        if (parsedWeekStart.isBefore(lowerBound)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    String.format("Tuần %s không hợp lệ. Giới hạn chọn trong vòng 1 năm", filterValue));
        }
    }

    /**
     * Validates the month range.
     * Allowed period: from (current month start - 1 day - 2 years) to current month.
     * <p>
     * Example: If today's date is 2025-02-05, effectiveToday becomes 2025-02-04.
     * Then currentMonth is derived from effectiveToday (e.g., 2025-02), and the lower bound is
     * computed from the first day of that month minus 1 day minus 2 years.
     *
     * @param filterValue The month string (e.g., "2025-02") to validate.
     */
    private static void validateMonthRange(String filterValue) {
        YearMonth parsedMonth = YearMonth.parse(filterValue, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate effectiveToday = LocalDate.now().minusDays(1);
        YearMonth currentMonth = YearMonth.from(effectiveToday);
        LocalDate currentMonthStart = currentMonth.atDay(1);
        LocalDate lowerBoundDate = currentMonthStart.minusDays(1).minusYears(2);
        YearMonth lowerBound = YearMonth.from(lowerBoundDate);

        if (parsedMonth.isAfter(currentMonth)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    String.format("Tháng %s không hợp lệ", filterValue));
        }
        if (parsedMonth.isBefore(lowerBound)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    String.format("Tháng %s không hợp lệ. Giới hạn chọn trong vòng 2 năm", filterValue));
        }
    }

    /**
     * Validates the quarter range.
     * Allowed period: from (current quarter start - 1 day - 5 years) to current quarter.
     * <p>
     * Example: If today's date is 2025-02-05, effectiveToday becomes 2025-02-04.
     * The current quarter is derived from effectiveToday (e.g., Q1-2025 if effectiveToday is in January–March),
     * and the lower bound is computed from the start of that quarter minus 1 day minus 5 years.
     *
     * @param filterValue The quarter string (e.g., "2025-Q1") to validate.
     */
    private static void validateQuarterRange(String filterValue) {
        String[] parts = filterValue.split("-Q");
        int parsedYear = Integer.parseInt(parts[0]);
        int parsedQuarter = Integer.parseInt(parts[1]);
        if (parsedQuarter < 1 || parsedQuarter > 4) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    String.format("Quý %d không hợp lệ, chỉ được từ 1 đến 4", parsedQuarter));
        }
        LocalDate effectiveToday = LocalDate.now().minusDays(1);
        int effectiveMonth = effectiveToday.getMonthValue();
        int currentQuarter = (effectiveMonth - 1) / 3 + 1;
        LocalDate currentQuarterStart = LocalDate.of(effectiveToday.getYear(), (currentQuarter - 1) * 3 + 1, 1);
        LocalDate lowerBoundDate = currentQuarterStart.minusDays(1).minusYears(5);
        int lowerBoundYear = lowerBoundDate.getYear();
        int lowerBoundQuarter = (lowerBoundDate.getMonthValue() - 1) / 3 + 1;
        int parsedQuarterValue = parsedYear * 4 + parsedQuarter;
        int currentQuarterValue = currentQuarterStart.getYear() * 4 + currentQuarter;
        int lowerBoundQuarterValue = lowerBoundYear * 4 + lowerBoundQuarter;

        if (parsedQuarterValue > currentQuarterValue) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    String.format("Quý %s không hợp lệ", filterValue));
        }
        if (parsedQuarterValue < lowerBoundQuarterValue) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    String.format("Quý %s không hợp lệ. Giới hạn chọn trong vòng 5 năm", filterValue));
        }
    }

    /**
     * Validates the year range.
     * Allowed period: from (current year start - 1 day - 10 years) to current year.
     * <p>
     * Example: If today's date is 2025-02-05, effectiveToday becomes 2025-02-04.
     * Then the current year is derived from effectiveToday (e.g., 2025), and the lower bound
     * is computed from the start of that year minus 1 day minus 10 years.
     *
     * @param filterValue The year string (e.g., "2025") to validate.
     */
    private static void validateYearRange(String filterValue) {
        int parsedYear = Integer.parseInt(filterValue);
        LocalDate effectiveToday = LocalDate.now().minusDays(1);
        LocalDate currentYearStart = LocalDate.of(effectiveToday.getYear(), 1, 1);
        LocalDate lowerBoundDate = currentYearStart.minusDays(1).minusYears(10);
        int lowerBoundYear = lowerBoundDate.getYear();
        int currentYear = effectiveToday.getYear();
        if (parsedYear > currentYear) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    String.format("Năm %s không hợp lệ.", filterValue));
        }
        if (parsedYear < lowerBoundYear) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    String.format("Năm %s không hợp lệ. Giới hạn chọn trong vòng 10 năm", filterValue));
        }
    }
}
