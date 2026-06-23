package com.smartbank.repository;

import com.smartbank.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.senderWallet.id = :walletId OR t.receiverWallet.id = :walletId) " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findAllByWalletId(@Param("walletId") Long walletId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.senderWallet.id = :walletId OR t.receiverWallet.id = :walletId) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletIdAndDateRange(
            @Param("walletId") Long walletId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.receiverWallet.id = :walletId AND t.status = 'SUCCESS' " +
           "AND t.transactionType IN ('CREDIT', 'ADD_MONEY', 'TRANSFER')")
    BigDecimal sumTotalReceived(@Param("walletId") Long walletId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.senderWallet.id = :walletId AND t.status = 'SUCCESS' " +
           "AND t.transactionType IN ('DEBIT', 'TRANSFER', 'WITHDRAWAL')")
    BigDecimal sumTotalSent(@Param("walletId") Long walletId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'SUCCESS'")
    long countSuccessfulTransactions();

    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
    List<Transaction> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as count, SUM(amount) as total " +
           "FROM transactions WHERE status = 'SUCCESS' AND created_at >= :startDate " +
           "GROUP BY DATE(created_at) ORDER BY date", nativeQuery = true)
    List<Object[]> getDailyTransactionStats(@Param("startDate") LocalDateTime startDate);

    @Query(value = "SELECT EXTRACT(MONTH FROM created_at) as month, COUNT(*) as count, SUM(amount) as total " +
           "FROM transactions WHERE status = 'SUCCESS' AND EXTRACT(YEAR FROM created_at) = :year " +
           "GROUP BY EXTRACT(MONTH FROM created_at) ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyTransactionStats(@Param("year") int year);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:type IS NULL OR t.transactionType = :type) AND " +
           "(:search = '' OR LOWER(t.transactionId) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.senderWallet.accountNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.receiverWallet.accountNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Transaction> searchAllTransactions(@Param("search") String search, 
                                            @Param("status") Transaction.TransactionStatus status, 
                                            @Param("type") Transaction.TransactionType type, 
                                            Pageable pageable);
}
