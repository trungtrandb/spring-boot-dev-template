package site.code4fun;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Test {
    public static void main(String[] args) {
        BigDecimal d1 = BigDecimal.valueOf(7.0);
        BigDecimal d2 = BigDecimal.valueOf(8.0);
        System.out.println(d1.divide(d2,1, RoundingMode.DOWN));
        System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }
}
