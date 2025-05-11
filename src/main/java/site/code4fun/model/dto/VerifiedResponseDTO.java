package site.code4fun.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifiedResponseDTO {

    @JsonProperty("total_tax")
    private BigDecimal totalTax;

    @JsonProperty("shipping_charge")
    private BigDecimal shippingCharge;

    @JsonProperty("unavailable_products")
    private List<Long> unavailableProducts;

    @JsonProperty("wallet_amount")
    private BigDecimal walletAmount;

    @JsonProperty("wallet_currency")
    private BigDecimal walletCurrency;
}
