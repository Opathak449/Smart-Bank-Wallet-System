package com.smartbank.service.impl;

import com.smartbank.dto.request.ChangePasswordRequest;
import com.smartbank.dto.request.CreateUserRequest;
import com.smartbank.dto.request.LoginRequest;
import com.smartbank.dto.request.RegisterRequest;
import com.smartbank.dto.request.UpdateProfileRequest;
import com.smartbank.dto.response.ApiResponse;
import com.smartbank.dto.response.AuthResponse;
import com.smartbank.dto.response.UserResponse;
import com.smartbank.entity.*;
import com.smartbank.exception.BadRequestException;
import com.smartbank.exception.ResourceNotFoundException;
import com.smartbank.repository.*;
import com.smartbank.security.JwtUtils;
import com.smartbank.service.EmailService;
import com.smartbank.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WalletRepository walletRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    @Transactional
    public ApiResponse<UserResponse> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .address(request.getAddress())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(User.UserStatus.ACTIVE)
                .emailVerified(false)
                .roles(Set.of(userRole))
                .build();

        user = userRepository.save(user);

        // Create Wallet
        String accountNumber = AccountNumberGenerator.generate(walletRepository);
        Wallet wallet = Wallet.builder()
                .accountNumber(accountNumber)
                .user(user)
                .balance(java.math.BigDecimal.ZERO)
                .status(Wallet.WalletStatus.ACTIVE)
                .build();
        walletRepository.save(wallet);

        // Send welcome email async
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        return ApiResponse.success("Registration successful! Welcome to SmartBank.", toUserResponse(user));
    }

    @Transactional
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == User.UserStatus.BLOCKED) {
            throw new BadRequestException("Account is blocked. Contact support.");
        }

        String accessToken = jwtUtils.generateJwtToken(user.getUsername());
        String refreshToken = generateRefreshToken(user);

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserResponse(user))
                .build();

        return ApiResponse.success("Login successful", response);
    }

    private String generateRefreshToken(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        return refreshTokenRepository.save(token).getToken();
    }

    @Transactional
    public ApiResponse<AuthResponse> refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (refreshToken.isExpired() || refreshToken.isRevoked()) {
            throw new BadRequestException("Refresh token expired. Please login again.");
        }
        String newAccessToken = jwtUtils.generateJwtToken(refreshToken.getUser().getUsername());
        AuthResponse response = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .user(toUserResponse(refreshToken.getUser()))
                .build();
        return ApiResponse.success("Token refreshed", response);
    }

    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(user ->
                refreshTokenRepository.deleteByUserId(user.getId()));
    }

    @Transactional
    public ApiResponse<UserResponse> createUserByAdmin(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new BadRequestException("Username already exists");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new BadRequestException("Email already exists");

        List<String> roleNames = (request.getRoles() == null || request.getRoles().isEmpty())
                ? List.of("ROLE_USER") : request.getRoles();
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roles.add(roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)));
        }

        User.UserStatus status = User.UserStatus.ACTIVE;
        if (request.getStatus() != null) {
            try { status = User.UserStatus.valueOf(request.getStatus().toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .address(request.getAddress())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(status)
                .emailVerified(true)
                .roles(roles)
                .build();
        user = userRepository.save(user);

        String accountNumber = AccountNumberGenerator.generate(walletRepository);
        Wallet wallet = Wallet.builder()
                .accountNumber(accountNumber)
                .user(user)
                .balance(java.math.BigDecimal.ZERO)
                .status(Wallet.WalletStatus.ACTIVE)
                .build();
        walletRepository.save(wallet);

        return ApiResponse.success("User created successfully", toUserResponse(user));
    }

    @Transactional
    public ApiResponse<UserResponse> updateUserRoles(Long userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (roleNames == null || roleNames.isEmpty())
            throw new BadRequestException("At least one role is required");

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roles.add(roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)));
        }
        user.setRoles(roles);
        userRepository.save(user);
        return ApiResponse.success("Roles updated successfully", toUserResponse(user));
    }

    @Transactional
    public ApiResponse<UserResponse> updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getMobileNumber() != null) user.setMobileNumber(request.getMobileNumber());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        userRepository.save(user);
        return ApiResponse.success("Profile updated successfully", toUserResponse(user));
    }

    @Transactional
    public ApiResponse<Void> changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.success("Password changed successfully", null);
    }

    public UserResponse toUserResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .address(user.getAddress())
                .profileImage(user.getProfileImage())
                .status(user.getStatus().name())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .createdAt(user.getCreatedAt());

        walletRepository.findByUserId(user.getId()).ifPresent(wallet -> {
            builder.walletAccountNumber(wallet.getAccountNumber());
            builder.walletBalance(wallet.getBalance());
            builder.walletStatus(wallet.getStatus().name());
        });

        return builder.build();
    }
}
