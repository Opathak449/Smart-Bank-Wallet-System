package com.smartbank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_wallet_id")
    private Wallet senderWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_wallet_id")
    private Wallet receiverWallet;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    private String description;
    private String remarks;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "balance_before", precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (transactionId == null) {
            transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        }
    }

    public enum TransactionType {
        CREDIT, DEBIT, TRANSFER, ADD_MONEY, WITHDRAWAL
    }

    public enum TransactionStatus {
        SUCCESS, FAILED, PENDING, REVERSED
    }
}
