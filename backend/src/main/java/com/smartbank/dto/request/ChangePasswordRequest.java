package com.smartbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String currentPassword;

    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;
}
