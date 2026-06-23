package com.smartbank.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String transactionId;
    private String senderAccountNumber;
    private String senderName;
    private String receiverAccountNumber;
    private String receiverName;
    private BigDecimal amount;
    private String transactionType;
    private String status;
    private String description;
    private String paymentMethod;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}
