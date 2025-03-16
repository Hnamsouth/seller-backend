package com.vtp.vipo.seller.common.utils;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;

public class PeriodUtil {

    /**
     * Lấy giá trị "cùng kỳ" (previous period) từ filterType, filterValue.
     *
     * @param filterType  day, week, month, quarter, year
     * @param filterValue Định dạng:
     *                    - day: yyyy-MM-dd (VD: 2024-12-02)
     *                    - week: yyyy-Wxx  (VD: 2024-W10)
     *                    - month: yyyy-MM  (VD: 2024-12)
     *                    - quarter: yyyy-Qx (VD: 2024-Q4)
     *                    - year: yyyy      (VD: 2024)
     * @return Giá trị cùng kỳ (previous period) tương ứng:
     * - day: lùi 1 ngày (2024-12-01)
     * - week: lùi 1 tuần (2024-W09)
     * - month: lùi 1 tháng (2024-11)
     * - quarter: lùi 1 quý (2024-Q3)
     * - year: lùi 1 năm (2023)
     */
    public static String getPreviousPeriod(String filterType, String filterValue) {
        return switch (filterType) {
            case "day" -> previousDay(filterValue);
            case "week" -> previousWeek(filterValue);
            case "month" -> previousMonth(filterValue);
            case "quarter" -> previousQuarter(filterValue);
            case "year" -> previousYear(filterValue);
            default -> throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    "Lọc theo không hợp lệ: " + filterType);
        };
    }

    // ---------------------------
    // day: yyyy-MM-dd => trừ 1 ngày
    private static String previousDay(String filterValue) {
        LocalDate date = LocalDate.parse(filterValue, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate previousDate = date.minusDays(1);
        return previousDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    // ---------------------------
    // week: yyyy-Wxx => trừ 1 tuần (sử dụng ISO week)
    private static String previousWeek(String filterValue) {
        // Lấy year và week number, ví dụ: "2024-W10"
        int year = Integer.parseInt(filterValue.substring(0, 4));
        int week = Integer.parseInt(filterValue.substring(6));  // từ vị trí 6 đến hết (W10 => "10")

        // Tạo 1 LocalDate từ year/week => set dayOfWeek = 1 (thứ Hai)
        // => trừ 1 tuần => lấy lại year/week
        LocalDate date = LocalDate.of(year, 1, 1)
                .with(WeekFields.ISO.weekOfYear(), week)
                .with(WeekFields.ISO.dayOfWeek(), 1)
                .minusWeeks(1);

        int newYear = date.get(WeekFields.ISO.weekBasedYear());
        int newWeek = date.get(WeekFields.ISO.weekOfYear());

        // format lại "yyyy-Wxx", zero-padding cho week (nếu < 10)
        return String.format("%04d-W%02d", newYear, newWeek);
    }

    // ---------------------------
    // month: yyyy-MM => trừ 1 tháng
    private static String previousMonth(String filterValue) {
        YearMonth ym = YearMonth.parse(filterValue, DateTimeFormatter.ofPattern("yyyy-MM"));
        YearMonth ymPrev = ym.minusMonths(1);
        return ymPrev.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    // ---------------------------
    // quarter: yyyy-Qx => trừ 1 quý
    // (nếu Q1 -> lùi sang Q4 của năm trước)
    private static String previousQuarter(String filterValue) {
        // Ví dụ: "2024-Q4" => year=2024, quarter=4
        String[] parts = filterValue.split("-Q");
        int year = Integer.parseInt(parts[0]);
        int quarter = Integer.parseInt(parts[1]);

        // Lùi 1 quý
        quarter -= 1;
        if (quarter < 1) {
            quarter = 4;
            year -= 1;
        }
        return year + "-Q" + quarter;
    }

    // ---------------------------
    // year: yyyy => trừ 1 năm
    private static String previousYear(String filterValue) {
        int year = Integer.parseInt(filterValue);
        return String.valueOf(year - 1);
    }
}
