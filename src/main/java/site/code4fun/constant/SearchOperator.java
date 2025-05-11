package site.code4fun.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SearchOperator {
    public static final String EQUAL = "eq";
    public static final String NOT_EQUAL = "ne";
    public static final String GREATER_THAN = "gt";
    public static final String LESS_THAN = "lt";
    public static final String IN = "in";
    public static final String NOT_IN = "not_in";
}
