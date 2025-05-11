package site.code4fun.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import site.code4fun.constant.DayOfWeek;
import site.code4fun.constant.WeekNumber;
import site.code4fun.model.Auditable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date and time operations.
 * This class provides thread-safe methods for date manipulation and formatting.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {
    private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern(ISO_FORMAT);

    /**
     * Gets the first date of the current month.
     *
     * @return LocalDate representing the first day of the current month
     */
    public static LocalDate getFirstDateOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * Gets the last date of the current month.
     *
     * @return LocalDate representing the last day of the current month
     */
    public static LocalDate getLastDateOfMonth() {
        return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    }

    /**
     * Gets the last item from a collection based on creation date.
     *
     * @param list Collection of Auditable items
     * @param <T> Type extending Auditable
     * @return The last item by creation date, or null if collection is empty
     */
    @Nullable
    public static <T extends Auditable> T getLastByDate(Collection<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        return list.stream()
                .max(Comparator.comparing(T::getCreated))
                .orElse(null);
    }

    /**
     * Calculates the difference in days between two dates.
     *
     * @param date1 First date
     * @param date2 Second date
     * @return Number of days between dates
     */
    public static long dateDiff(@Nullable Date date1, @Nullable Date date2) {
        if (date1 == null || date2 == null) {
            return 0;
        }
        long timeDiff = Math.abs(date2.getTime() - date1.getTime());
        return TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
    }

    /**
     * Formats a date using ISO format.
     *
     * @param date Date to format
     * @return Formatted date string, or null if input is null
     */
    @Nullable
    public static String formatDate(@Nullable Date date) {
        return formatDate(date, ISO_FORMAT);
    }

    /**
     * Formats a date using specified pattern.
     *
     * @param date Date to format
     * @param pattern Date format pattern
     * @return Formatted date string, or null if input is null
     */
    @Nullable
    public static String formatDate(@Nullable Date date, String pattern) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Gets the day of week as a string representation.
     *
     * @param date LocalDate to get day information from
     * @return String representation of the day (e.g., "FIRST_MONDAY")
     */
    public static String getDayAsString(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        int weekNumber = (date.getDayOfMonth() - 1) / 7 + 1;
        int dayOfWeek = date.getDayOfWeek().getValue();
        return WeekNumber.getWeekAsString(weekNumber) + "_" + DayOfWeek.getDayOfWeekAsString(dayOfWeek);
    }
}
