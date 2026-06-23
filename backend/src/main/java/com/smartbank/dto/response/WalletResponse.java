// WalletResponse.java
package com.smartbank.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WalletResponse {
    private Long id;
    private String walletId;
    private String accountNumber;
    private BigDecimal balance;
    private String currency;
    private String status;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private LocalDateTime createdAt;
    private String ownerName;
}
