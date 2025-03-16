package com.vtp.vipo.seller.common.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author : Le Quang Dat </br>
 * Email: quangdat0993@gmail.com</br>
 * Jan 20, 2024
 */
public class DateUtils {

    private static Logger logger = LoggerFactory.getLogger(DateUtils.class);

    public static final String ddMMyyyy = "dd/MM/yyyy";

    public static final String ddMMyyyyHHmmSS = "dd/MM/yyyy HH:mm:ss";

    public static final String ddMMyyyyHHmm = "dd/MM/yyyy HH:mm";

    public static final String DD_MM_YYYY_HH_MM_SS = "dd/MM/yyyy HH:mm:ss";

    public static final String dd_MM_yyyy_HH_mm_SS = "dd_MM_yyyy_HH_mm_ss";

    public static final String HHmmSSddMMyyyy = "HH:mm:ss dd/MM/yyyy";

    public static final String DATE_TIME_MYSQL_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String SB_RESERVATION_DATE_FORMAT = "yyyy-M-dd HH:mm:ss";

    public static final String HHmmddMM = "HH:mm dd/MM";

    public static final String yyyyMMddHHmmssSSS = "yyyyMMddHHmmssSSS";

    public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";

    public static final String yyyyMMdd = "yyyyMMdd";

    public static final String salesDateFormat = "yyyy/MM/dd";

    public static final String salesMonthFormat = "yyyy/MM";

    public static final String HHmmFormat = "HH:mm";

    public static final String yyyyMMddThhMMssZ = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final String AMAZON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final String MINUTE_START_FORMAT = "yyyy-MM-dd HH:mm:00";

    public static final String MINUTE_END_FORMAT = "yyyy-MM-dd HH:mm:59";

    public static final String saleSDateCheckoutFormat = "yyyy-MM-dd HH:mm";

    public static final String HHmmss = "HH:mm:ss";

    public static final String yyyyMMddHHmm = "yyyyMMddHHmm";

    public static final String yyyyMM = "yyyyMM";

    public static final String yyyy_MM = "yyyy-MM";

    public static final String yyyy_MM_dd = "yyyy-MM-dd";

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy/MM/dd/hh/mm/ss";

    public static final String HH_MM_DD_MM_YYYY = "HH:mm dd/MM/yyyy";

    public static final String ISO_8601_EXTENDED_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static String toDateString(Date date, String format) {
        if (date == null || StringUtils.hasLength(format))
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String toStringFromLong(Long date, String format){
        if(date == null)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static Long getTimeInSeconds(LocalDateTime localDateTime) {
        if (!ObjectUtils.isEmpty(localDateTime)) {
            return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
        }
        return null;
    }

    public static String toDateStringFromTimeStamp(Timestamp date, String format) {
        if (date == null || StringUtils.hasLength(format))
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String convertStringFormatToOtherFormat(String dateStr, String firstFormat, String afterFormat) {
        if (StringUtils.hasLength(dateStr) || StringUtils.hasLength(firstFormat) || StringUtils.hasLength(afterFormat))
            return "";
        Date date = toDateFromStr(dateStr, firstFormat);
        return toDateString(date, afterFormat);
    }


    public static Date getCurrentTime() {
        return new Date();
    }

    public static Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    public static Date getBeforeDate(int before) {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -before);
        return cal.getTime();
    }

    public static Date getNextDate(int next) {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, +next);
        return cal.getTime();
    }

    /**
     * Now : truncated hour
     *
     * @return Timestamp
     */
//    public static Timestamp before(long days) {
//        return Timestamp.valueOf(LocalDateTime.now().minusDays(days).truncatedTo(ChronoUnit.HOURS));
//    }

    public static Date beginOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public static Timestamp beginOfDay(Timestamp timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp.getTime());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new Timestamp(cal.getTimeInMillis());
    }

    public static Date endOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    public static String getCurrentTimeString(String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(new Date());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static Date toDateFromStr(String dateString, String format) {
        DateFormat dateTimeFormat = new SimpleDateFormat(format);
        try {
            Date date = dateTimeFormat.parse(dateString);
            return date;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static Timestamp toTimestampFromStr(String dateString, String format) {
        DateFormat dateTimeFormat = new SimpleDateFormat(format);
        try {
            Date date = dateTimeFormat.parse(dateString);
            return new Timestamp(date.getTime());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static List<Date> getDatesBetweenTwoDates(Date startDate, Date endDate) {
        List<Date> datesInRange = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);

        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(endDate);

        while (calendar.before(endCalendar) || calendar.equals(endCalendar)) {
            Date result = calendar.getTime();
            datesInRange.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        return datesInRange;
    }

    public static boolean isDateSame(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat(salesDateFormat);
        return fmt.format(date1).equals(fmt.format(date2));
    }

    public static String converTimeToString(Time time, String format) {
        Date date = new Date();
        date.setTime(time.getTime());
        return new SimpleDateFormat(format).format(date);
    }
    public static String converTimeToString(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static Date getFirstDateOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return beginOfDay(cal.getTime());
    }

    public static Date getLastDatefMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return endOfDay(cal.getTime());
    }

    public static boolean isValidFormat(String format, String value) {
        LocalDateTime ldt;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        try {
            ldt = LocalDateTime.parse(value, formatter);
            String result = ldt.format(formatter);
            return result.equals(value);
        } catch (DateTimeParseException e) {
            try {
                LocalDate ld = LocalDate.parse(value, formatter);
                String result = ld.format(formatter);
                return result.equals(value);
            } catch (DateTimeParseException exp) {
                try {
                    LocalTime lt = LocalTime.parse(value, formatter);
                    String result = lt.format(formatter);
                    return result.equals(value);
                } catch (DateTimeParseException e2) {
                    logger.error(e.getMessage(), e);
                    // Debugging purposes
                    //e2.printStackTrace();
                }
            }
        }
        return false;
    }

    public static Timestamp toTimeFromTime(Timestamp time, String format) {
        String date = toDateStringFromTimeStamp(time, format);
        return toTimestampFromStr(date, format);
    }

    public static int compareDate(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat(salesDateFormat);
        return fmt.format(date1).compareTo(fmt.format(date2));
    }

    public static Date addHoursToDate(Date date, int hours) {
        Calendar calenda = Calendar.getInstance();
        calenda.setTime(date);
        calenda.add(Calendar.HOUR_OF_DAY, hours);
        return calenda.getTime();
    }

    public static int hoursDifference(Date date1, Date date2) {

        final int MILLI_TO_HOUR = 1000 * 60 * 60;
        return (int) (date1.getTime() - date2.getTime()) / MILLI_TO_HOUR;
    }

    public static Date subtractDay(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }

    public static Date getDateOneMonthAgo(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        Date result = cal.getTime();
        return result;
    }

    public static Timestamp getDateMonthAgo(Timestamp date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        Date result = cal.getTime();
        return new Timestamp(result.getTime());
    }

    public static Long getFirstDateOfMonthInSecond(LocalDateTime date){
        return getTimeInSeconds(date.with(TemporalAdjusters.firstDayOfMonth()));
    }

    public static Long getLastDateOfMonthInSecond(LocalDateTime date){
        return getTimeInSeconds(date.with(TemporalAdjusters.lastDayOfMonth()));
    }

    public static Long getFirstDateOfWeekInSecond(LocalDateTime date){
        return getTimeInSeconds(date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
    }

    public static Long getLastDateOfWeekInSecond(LocalDateTime date){
        return getTimeInSeconds(date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
    }

    public static Time valueOf(int hour, int minutes, int second) {
        return Time.valueOf(toLocalTime(hour, minutes, second));
    }

    public static LocalTime toLocalTime(int hour, int minutes, int second) {
        return LocalTime.of(hour, minutes, second);
    }

    public static int getHours(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinutes(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MINUTE);
    }

    public static int getSeconds(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.SECOND);
    }

    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }

    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public static Timestamp getCurrentTimeMillis() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static Timestamp addDays(long days, Timestamp t1) throws Exception {
        if (days < 0) {
            throw new Exception("Day in wrong format.");
        }
        Long milliseconds = days * 24 * 60 * 60 * 1000;
        return new Timestamp(t1.getTime() + milliseconds);
    }

    public static Timestamp addHours(long hours, Timestamp t1) throws Exception {
        if (hours < 0) {
            throw new Exception("Day in wrong format.");
        }
        Long milliseconds = hours * 60 * 60 * 1000;
        return new Timestamp(t1.getTime() + milliseconds);
    }

    public static Time toTimeSQL(String value) throws DateTimeParseException {
        LocalTime localTime = LocalTime.parse(value, DateTimeFormatter.ofPattern(HHmmFormat));
        return Time.valueOf(localTime);
    }

    public static int getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static LocalTime convertTimeToLocalTime(Time time, String format) {
        return LocalTime.parse(time.toString(), DateTimeFormatter.ofPattern(format));
    }

    public static boolean isValidateFormat(String format, String value) {
        if (StringUtils.hasLength(value)) {
            return true;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            simpleDateFormat.parse(value);
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Timestamp getStartOfNextDay(Timestamp timestamp) {
        if (timestamp != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp.getTime());
            cal.add(Calendar.DATE, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return new Timestamp(cal.getTimeInMillis());
        }
        return null;
    }

    public static boolean checkSameLocalTime(LocalTime time1, LocalTime time2) {
        return (time1.compareTo(time2) == 0) ? true : false;
    }

    public static Date plusOneDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    public static Date plusOneMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, 1);
        return c.getTime();
    }

    public static Date parseMysqlDatetime(String value) {
        if (!StringUtils.hasLength(value) && isValidFormat(DATE_TIME_MYSQL_FORMAT, value)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_MYSQL_FORMAT);
                return sdf.parse(value);
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
                logger.debug("{} is not mysql datetime format.", value);
            }
        }
        return null;
    }

    public static Date parseSbReservationDate(String value) {
        if (!StringUtils.hasLength(value) && isValidFormat(SB_RESERVATION_DATE_FORMAT, value)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(SB_RESERVATION_DATE_FORMAT);
                return sdf.parse(value);
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static boolean isValidateMonth(String month) {
        if (StringUtils.hasLength(month)) return false;
        String number = "[0-9]*";
        Pattern pattern = Pattern.compile(number);
        Matcher matcher = pattern.matcher(month);
        if (!matcher.matches() || Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) {
            return false;
        }
        return true;
    }

    public static Date getNextDayFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        date = calendar.getTime();
        return date;
    }

    public static Date setHoursForDate(Date date, int hours) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, hours);
        return c.getTime();
    }

    public static Date setMinutesForDate(Date date, int minutes) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MINUTE, minutes);
        return c.getTime();
    }

    public static Date setSecondsForDate(Date date, int seconds) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.SECOND, seconds);
        return c.getTime();
    }

    public static Date setTimeForDate(Date date, int hours, int minutes, int seconds) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.SECOND, seconds);
        return c.getTime();
    }

    public static long getCurrentTimeInSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static Long convertMilTimeToSecond(Long time){
        if(!ObjectUtils.isEmpty(time) && time > 0){

            int timeLenghts = String.valueOf(time).length();

            if(timeLenghts == 10){
                return time;
            }

            return time / 1000;
        }
        return time;
    }
    public static Instant truncateToMidnight(Instant instant) {
        // Chuyển Instant thành LocalDate (phần ngày tháng, bỏ qua thời gian)
        LocalDate localDate = instant.atZone(ZoneOffset.UTC).toLocalDate();
        // Thiết lập thời gian về 00:00:00
        LocalDateTime startOfDay = localDate.atStartOfDay();
        // Chuyển lại thành Instant với thời gian 00:00:00
        return startOfDay.toInstant(ZoneOffset.UTC);
    }

    /**
     * Retrieves the current local date and time based on the system's default time zone.
     * <p>
     * This method leverages {@link LocalDateTime#now()} to obtain the current date and time.
     * It does not account for any specific time zone other than the system's default.
     * </p>
     *
     * @return the current {@link LocalDateTime} instance.
     */
    public static LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now();
    }

    /**
     * Converts epoch seconds to {@link LocalDateTime} based on the system's default time zone.
     * <p>
     * Epoch seconds represent the number of seconds that have elapsed since
     * 00:00:00 Coordinated Universal Time (UTC), Thursday, 1 January 1970.
     * This method converts the given epoch seconds to a {@link LocalDateTime} instance.
     * </p>
     *
     * @param epochSeconds the number of seconds since the epoch (1970-01-01T00:00:00Z).
     *                     Must not be {@code null}.
     * @return the corresponding {@link LocalDateTime} instance.
     * @throws DateTimeException if the resulting date-time exceeds the supported range.
     * @throws NullPointerException if {@code epochSeconds} is {@code null}.
     */
    public static LocalDateTime getLocalDateTimeFromEpochSecond(long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }

    /**
     * Converts epoch seconds to a formatted date string based on the provided pattern.
     *
     * <p>This method takes an epoch time in seconds and converts it to a human-readable date string
     * according to the specified date pattern. It uses the system's default time zone for the conversion.
     *
     * @param epochSeconds the epoch time in seconds (number of seconds since January 1, 1970, 00:00:00 GMT)
     * @param pattern      the date pattern string following {@link DateTimeFormatter} patterns (e.g., "yyyy-MM-dd")
     * @return a formatted date string representing the given epoch time
     * @throws IllegalArgumentException if the provided pattern is invalid
     */
    public static String convertEpochToDateString(long epochSeconds, String pattern) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * Retrieves the current instant from the system clock.
     *
     * <p>This method returns the current moment from the system clock in UTC. It is useful for
     * obtaining a timestamp that can be used for logging, measuring time intervals, or
     * recording the exact moment an event occurs.
     *
     * @return the current {@link Instant} representing the current moment in UTC
     */
    public static Instant getCurrentInstant() {
        return Instant.now();
    }

    public static String toDateString(LocalDateTime date, String format) {
        if (ObjectUtils.isEmpty(date))
            return null;
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime convertEpochSecondsToLocalDateTime(long epochSeconds) {
        if (epochSeconds == 0)      //when the time is 0 then there is no time
            return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }

    public static LocalDateTime convertEpochSecondsToLocalDateTime(Long epochSeconds) {
        if (DataUtils.isNullOrEmpty(epochSeconds) || epochSeconds == 0)      //when the time is 0 then there is no time
            return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }

    public static String toDateString(LocalDate localDate, String format) {
        if (localDate == null || StringUtils.hasLength(format))
            return "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        return localDate.format(formatter);
    }


    /**
     * Converts a LocalDateTime to epoch seconds.
     *
     * @param localDateTime the LocalDateTime to convert
     * @return the epoch seconds corresponding to the given LocalDateTime and ZoneId
     */
    public static Long convertToEpochSeconds(LocalDateTime localDateTime) {
        if (ObjectUtils.isEmpty(localDateTime))
            return null;
        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public static LocalDateTime convertToStartOfDay(Long epochSeconds){

        LocalDateTime date = convertEpochSecondsToLocalDateTime(epochSeconds);
        if (date == null)
            return null;

        return date.toLocalDate().atTime(0, 0, 0);
    }

    public static LocalDateTime convertToEndOfDay(Long epochSeconds){

        LocalDateTime date = convertEpochSecondsToLocalDateTime(epochSeconds);
        if (date == null)
            return null;

        return date.toLocalDate().atTime(23, 59, 59);
    }

    public static Long convertToStartOfDayByEpochSeconds(Long epochSeconds){

        LocalDateTime date = convertToStartOfDay(epochSeconds);
        if (date == null)
            return null;

        return getTimeInSeconds(date);
    }

    public static Long convertToEndOfDayByEpochSeconds(Long epochSeconds){

        LocalDateTime date = convertToEndOfDay(epochSeconds);
        if (date == null)
            return null;

        return getTimeInSeconds(date);
    }

    // Helper: Tính year-week
    public static YearWeek getYearWeek(LocalDate d) {
        int y = d.getYear();
        int w = d.get(WeekFields.ISO.weekOfYear());
        return new YearWeek(y, w);
    }

    // record để gói year-week
    public record YearWeek(int year, int week) {
    }

    public static Long convertLocalDateToEpochSeconds(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }
}
