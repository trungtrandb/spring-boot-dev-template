package site.code4fun.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class FinancialRatioResponse {
    private List<Map<String, Object>> ratio;
}
