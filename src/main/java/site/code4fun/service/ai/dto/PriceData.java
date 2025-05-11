package site.code4fun.service.ai.dto;

import java.time.LocalDate;

public record PriceData(LocalDate date, double open, double high, double low, double close, long volume) {
}