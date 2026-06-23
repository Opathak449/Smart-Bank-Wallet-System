package com.smartbank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Wallet {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", unique = true, nullable = false)
    private String walletId;

    @Column(name = "account_number", unique = true, nullable = false, length = 16)
    private String accountNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    @Column(length = 10)
    private String currency = "INR";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private WalletStatus status = WalletStatus.ACTIVE;

    @Builder.Default
    @Column(name = "daily_limit", precision = 15, scale = 2)
    private BigDecimal dailyLimit = new BigDecimal("50000.00");

    @Builder.Default
    @Column(name = "monthly_limit", precision = 15, scale = 2)
    private BigDecimal monthlyLimit = new BigDecimal("500000.00");

    @Column(name = "wallet_pin")
    private String walletPin;

    @OneToMany(mappedBy = "senderWallet", fetch = FetchType.LAZY)
    private List<Transaction> sentTransactions;

    @OneToMany(mappedBy = "receiverWallet", fetch = FetchType.LAZY)
    private List<Transaction> receivedTransactions;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (walletId == null) {
            walletId = "WLT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    public enum WalletStatus {
        ACTIVE, INACTIVE, BLOCKED, FROZEN
    }
}
