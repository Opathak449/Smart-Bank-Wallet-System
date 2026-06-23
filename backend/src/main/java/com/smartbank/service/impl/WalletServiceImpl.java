package com.smartbank.service.impl;

import com.smartbank.dto.request.AddMoneyRequest;
import com.smartbank.dto.request.TransferRequest;
import com.smartbank.dto.response.ApiResponse;
import com.smartbank.dto.response.TransactionResponse;
import com.smartbank.dto.response.WalletResponse;
import com.smartbank.entity.*;
import com.smartbank.exception.BadRequestException;
import com.smartbank.exception.InsufficientBalanceException;
import com.smartbank.exception.ResourceNotFoundException;
import com.smartbank.repository.*;
import com.smartbank.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("null")
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public ApiResponse<WalletResponse> getWalletByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        return ApiResponse.success("Wallet fetched", toWalletResponse(wallet));
    }

    @Transactional
    public ApiResponse<TransactionResponse> addMoney(String username, AddMoneyRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (wallet.getStatus() != Wallet.WalletStatus.ACTIVE) {
            throw new BadRequestException("Wallet is not active");
        }

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.setBalance(balanceBefore.add(request.getAmount()));
        walletRepository.save(wallet);

        Transaction txn = Transaction.builder()
                .receiverWallet(wallet)
                .amount(request.getAmount())
                .transactionType(Transaction.TransactionType.ADD_MONEY)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Money added via " + request.getPaymentMethod())
                .paymentMethod(request.getPaymentMethod())
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .build();
        txn = transactionRepository.save(txn);

        emailService.sendAddMoneyEmail(user.getEmail(), user.getFirstName(), request.getAmount(), wallet.getBalance());
        createNotification(user,
                "Money Added Successfully",
                "₹" + request.getAmount().toPlainString() + " added to your wallet. New balance: ₹" + wallet.getBalance().toPlainString(),
                Notification.NotificationType.SUCCESS);

        return ApiResponse.success("Money added successfully", toTransactionResponse(txn));
    }

    @Transactional
    public ApiResponse<TransactionResponse> transferMoney(String username, TransferRequest request) {
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Wallet senderWallet = walletRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender wallet not found"));
        Wallet receiverWallet = walletRepository.findByAccountNumber(request.getReceiverAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver wallet not found"));

        if (senderWallet.getAccountNumber().equals(request.getReceiverAccountNumber())) {
            throw new BadRequestException("Cannot transfer to your own account");
        }
        if (senderWallet.getStatus() != Wallet.WalletStatus.ACTIVE) {
            throw new BadRequestException("Sender wallet is not active");
        }
        if (receiverWallet.getStatus() != Wallet.WalletStatus.ACTIVE) {
            throw new BadRequestException("Receiver wallet is not active");
        }
        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        if (senderWallet.getWalletPin() != null && !senderWallet.getWalletPin().isEmpty()) {
            if (request.getPin() == null || !passwordEncoder.matches(request.getPin(), senderWallet.getWalletPin())) {
                throw new BadRequestException("Invalid MPIN. Please try again.");
            }
        }

        BigDecimal senderBefore = senderWallet.getBalance();
        BigDecimal receiverBefore = receiverWallet.getBalance();

        senderWallet.setBalance(senderBefore.subtract(request.getAmount()));
        receiverWallet.setBalance(receiverBefore.add(request.getAmount()));
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        Transaction txn = Transaction.builder()
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .amount(request.getAmount())
                .transactionType(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .balanceBefore(senderBefore)
                .balanceAfter(senderWallet.getBalance())
                .build();
        txn = transactionRepository.save(txn);

        emailService.sendTransferEmail(
                sender.getEmail(), sender.getFirstName(),
                receiverWallet.getUser().getFullName(),
                request.getAmount(), senderWallet.getBalance());

        createNotification(sender,
                "Transfer Successful",
                "₹" + request.getAmount().toPlainString() + " sent to " + receiverWallet.getUser().getFullName() + ". Balance: ₹" + senderWallet.getBalance().toPlainString(),
                Notification.NotificationType.SUCCESS);
        createNotification(receiverWallet.getUser(),
                "Money Received",
                "₹" + request.getAmount().toPlainString() + " received from " + sender.getFullName(),
                Notification.NotificationType.INFO);

        return ApiResponse.success("Transfer successful", toTransactionResponse(txn));
    }

    @Transactional(readOnly = true)
    public ApiResponse<Page<TransactionResponse>> getTransactionHistory(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        Page<TransactionResponse> page = transactionRepository
                .findAllByWalletId(wallet.getId(), pageable)
                .map(this::toTransactionResponse);
        return ApiResponse.success("Transactions fetched", page);
    }

    @Transactional(readOnly = true)
    public ApiResponse<Map<String, String>> lookupAccount(String accountNumber) {
        Wallet wallet = walletRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        Map<String, String> result = new HashMap<>();
        result.put("name", wallet.getUser().getFullName());
        result.put("accountNumber", accountNumber);
        return ApiResponse.success("Account found", result);
    }

    @Transactional
    public ApiResponse<String> setPin(String username, String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new BadRequestException("PIN must be exactly 4 digits");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        wallet.setWalletPin(passwordEncoder.encode(pin));
        walletRepository.save(wallet);
        return ApiResponse.success("MPIN set successfully", null);
    }

    private void createNotification(User user, String title, String message, Notification.NotificationType type) {
        try {
            notificationRepository.save(Notification.builder()
                    .user(user).title(title).message(message).type(type).build());
        } catch (Exception e) {
            log.error("Failed to create notification: {}", e.getMessage());
        }
    }

    public WalletResponse toWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .walletId(wallet.getWalletId())
                .accountNumber(wallet.getAccountNumber())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus().name())
                .dailyLimit(wallet.getDailyLimit())
                .monthlyLimit(wallet.getMonthlyLimit())
                .createdAt(wallet.getCreatedAt())
                .ownerName(wallet.getUser().getFullName())
                .build();
    }

    public TransactionResponse toTransactionResponse(Transaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .transactionId(txn.getTransactionId())
                .amount(txn.getAmount())
                .transactionType(txn.getTransactionType().name())
                .status(txn.getStatus().name())
                .description(txn.getDescription())
                .paymentMethod(txn.getPaymentMethod())
                .balanceBefore(txn.getBalanceBefore())
                .balanceAfter(txn.getBalanceAfter())
                .createdAt(txn.getCreatedAt())
                .senderAccountNumber(txn.getSenderWallet() != null ? txn.getSenderWallet().getAccountNumber() : null)
                .senderName(txn.getSenderWallet() != null ? txn.getSenderWallet().getUser().getFullName() : null)
                .receiverAccountNumber(txn.getReceiverWallet() != null ? txn.getReceiverWallet().getAccountNumber() : null)
                .receiverName(txn.getReceiverWallet() != null ? txn.getReceiverWallet().getUser().getFullName() : null)
                .build();
    }
}
