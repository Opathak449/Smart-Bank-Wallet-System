// ============================================================
// AdminController.java
// ============================================================
package com.smartbank.controller;

import com.smartbank.dto.request.CreateUserRequest;
import com.smartbank.dto.response.ApiResponse;
import com.smartbank.dto.response.UserResponse;
import com.smartbank.entity.User;
import com.smartbank.exception.ResourceNotFoundException;
import com.smartbank.repository.*;
import com.smartbank.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartbank.dto.response.TransactionResponse;
import com.smartbank.entity.Transaction;
import com.smartbank.service.impl.WalletServiceImpl;

@SuppressWarnings("null")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuthServiceImpl authService;
    private final WalletServiceImpl walletService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countActiveUsers());
        stats.put("totalTransactions", transactionRepository.count());
        stats.put("successfulTransactions", transactionRepository.countSuccessfulTransactions());
        BigDecimal totalBalance = walletRepository.sumAllActiveBalances();
        stats.put("totalWalletBalance", totalBalance != null ? totalBalance : BigDecimal.ZERO);

        int currentYear = java.time.LocalDate.now().getYear();
        stats.put("monthlyStats", transactionRepository.getMonthlyTransactionStats(currentYear));
        return ResponseEntity.ok(ApiResponse.success("Dashboard data fetched", stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserResponse> users;
        if (!search.isBlank()) {
            users = userRepository.searchUsers(search, pageable).map(authService::toUserResponse);
        } else {
            users = userRepository.findAll(pageable).map(authService::toUserResponse);
        }
        return ResponseEntity.ok(ApiResponse.success("Users fetched", users));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(authService.createUserByAdmin(request));
    }

    @PutMapping("/users/{id}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(
            @PathVariable Long id, @RequestBody List<String> roles) {
        return ResponseEntity.ok(authService.updateUserRoles(id, roles));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id, @RequestParam String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(User.UserStatus.valueOf(status.toUpperCase()));
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User status updated", authService.toUserResponse(user)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<?>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched",
                auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAllTransactions(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Transaction.TransactionStatus txnStatus = (status != null && !status.isBlank()) ? Transaction.TransactionStatus.valueOf(status.toUpperCase()) : null;
        Transaction.TransactionType txnType = (type != null && !type.isBlank()) ? Transaction.TransactionType.valueOf(type.toUpperCase()) : null;
        String searchQuery = (search != null && !search.isBlank()) ? search : "";

        Page<TransactionResponse> transactions = transactionRepository
                .searchAllTransactions(searchQuery, txnStatus, txnType, pageable)
                .map(walletService::toTransactionResponse);

        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", transactions));
    }
}
