// ============================================================
// FILE: repository/WalletRepository.java
// ============================================================
package com.smartbank.repository;

import com.smartbank.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(Long userId);
    Optional<Wallet> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.status = 'ACTIVE'")
    BigDecimal sumAllActiveBalances();
}
