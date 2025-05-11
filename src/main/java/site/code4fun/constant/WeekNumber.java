package site.code4fun.constant;

import lombok.Getter;

@Getter
public enum WeekNumber {
    N_A("N/A"), FIRST("FIRST"), SECOND("SECOND"), THIRD("THIRD"), FOURTH("FOURTH"), FIFTH("FIFTH");

    private final String value;

    WeekNumber(String value) {
        this.value = value;
    }

    public static String getWeekAsString(int number) {
        if (number <= 0) return String.valueOf(number);
        return (number <= 5) ? values()[number].getValue() : N_A.getValue();
    }
}
