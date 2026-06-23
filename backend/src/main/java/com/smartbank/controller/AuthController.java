// ============================================================
// AuthController.java
// ============================================================
package com.smartbank.controller;

import com.smartbank.dto.request.ChangePasswordRequest;
import com.smartbank.dto.request.LoginRequest;
import com.smartbank.dto.request.RegisterRequest;
import com.smartbank.dto.request.UpdateProfileRequest;
import com.smartbank.dto.response.ApiResponse;
import com.smartbank.dto.response.AuthResponse;
import com.smartbank.dto.response.UserResponse;
import com.smartbank.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestParam String token) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Principal principal) {
        authService.logout(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Principal principal, @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(principal.getName(), request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Principal principal, @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(principal.getName(), request));
    }
}
