// TransferRequest.java
package com.smartbank.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotBlank private String receiverAccountNumber;
    @NotNull @Positive @DecimalMin("1.00")
    private BigDecimal amount;
    private String description;
    private String pin;
}
