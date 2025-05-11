package site.code4fun.service.ai.dto;

public record BollingerBandsValue(double middle, double upper, double lower) {
        public static BollingerBandsValue nan() {
            return new BollingerBandsValue(Double.NaN, Double.NaN, Double.NaN);
        }

        @Override
        public String toString() {
            return String.format("BB[M:%.2f, U:%.2f, L:%.2f]", middle, upper, lower);
        }
    }