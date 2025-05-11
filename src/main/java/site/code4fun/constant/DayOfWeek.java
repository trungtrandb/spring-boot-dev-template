package site.code4fun.constant;

import lombok.Getter;

import java.util.Calendar;

@Getter
public enum DayOfWeek {
        N_A("N/A"), SUNDAY("SUNDAY"), MONDAY("MONDAY"), TUESDAY("TUESDAY"),
        WEDNESDAY("WEDNESDAY"), THURSDAY("THURSDAY"), FRIDAY("FRIDAY"), SATURDAY("SATURDAY");

        private final String value;

        DayOfWeek(String value) {
            this.value = value;
        }

    public static String getDayOfWeekAsString(int number) {
            return (number >= Calendar.SUNDAY && number <= Calendar.SATURDAY)
                    ? values()[number].getValue()
                    : N_A.getValue();
        }
    }