package com.smartbank.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddMoneyRequest {
    @NotNull @Positive @DecimalMin("1.00")
    private BigDecimal amount;
    @NotBlank private String paymentMethod; // DEBIT_CARD, CREDIT_CARD, UPI, NET_BANKING
    private String paymentReference;
}
