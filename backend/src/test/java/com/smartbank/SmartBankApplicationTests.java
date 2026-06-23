package com.smartbank;

import com.smartbank.dto.request.RegisterRequest;
import com.smartbank.dto.request.TransferRequest;
import com.smartbank.entity.*;
import com.smartbank.exception.BadRequestException;
import com.smartbank.exception.InsufficientBalanceException;
import com.smartbank.repository.*;
import com.smartbank.security.JwtUtils;
import com.smartbank.service.EmailService;
import com.smartbank.service.impl.AuthServiceImpl;
import com.smartbank.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmartBankApplicationTests {

    @Mock UserRepository userRepository;
    @Mock WalletRepository walletRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock RoleRepository roleRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authManager;
    @Mock JwtUtils jwtUtils;
    @Mock EmailService emailService;

    @InjectMocks AuthServiceImpl authService;
    @InjectMocks WalletServiceImpl walletService;

    @Test
    @DisplayName("Register fails when email already exists")
    void registerEmailDuplicate() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("existing@test.com");
        req.setPassword("Pass@123");
        req.setConfirmPassword("Pass@123");
        req.setFirstName("Test");
        req.setLastName("User");
        req.setMobileNumber("9876543210");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("Register fails when passwords do not match")
    void registerPasswordMismatch() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("user1");
        req.setEmail("user1@test.com");
        req.setPassword("Pass@123");
        req.setConfirmPassword("Different@123");
        req.setFirstName("Test");
        req.setLastName("User");
        req.setMobileNumber("9876543210");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Passwords do not match");
    }

    @Test
    @DisplayName("Transfer fails when insufficient balance")
    void transferInsufficientBalance() {
        User sender = User.builder().id(1L).username("sender").build();
        User receiver = User.builder().id(2L).username("receiver").build();

        Wallet senderWallet = Wallet.builder()
                .id(1L).user(sender).balance(new BigDecimal("100.00"))
                .accountNumber("1000000000000001").status(Wallet.WalletStatus.ACTIVE).build();
        Wallet receiverWallet = Wallet.builder()
                .id(2L).user(receiver).balance(new BigDecimal("500.00"))
                .accountNumber("1000000000000002").status(Wallet.WalletStatus.ACTIVE).build();

        TransferRequest req = new TransferRequest();
        req.setReceiverAccountNumber("1000000000000002");
        req.setAmount(new BigDecimal("500.00"));

        when(userRepository.findByUsername("sender")).thenReturn(Optional.of(sender));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByAccountNumber("1000000000000002")).thenReturn(Optional.of(receiverWallet));

        assertThatThrownBy(() -> walletService.transferMoney("sender", req))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("Transfer fails when sending to self")
    void transferToSelf() {
        User user = User.builder().id(1L).username("user").build();
        Wallet wallet = Wallet.builder()
                .id(1L).user(user).balance(new BigDecimal("1000.00"))
                .accountNumber("1000000000000001").status(Wallet.WalletStatus.ACTIVE).build();

        TransferRequest req = new TransferRequest();
        req.setReceiverAccountNumber("1000000000000001");
        req.setAmount(new BigDecimal("100.00"));

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.findByAccountNumber("1000000000000001")).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.transferMoney("user", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot transfer to your own account");
    }
}
