// ============================================================
// FILE: dto/request/RegisterRequest.java
// ============================================================
package com.smartbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Size(min=2, max=50)
    private String firstName;
    @NotBlank @Size(min=2, max=50)
    private String lastName;
    @NotBlank @Size(min=3, max=50)
    private String username;
    @NotBlank @Email
    private String email;
    @NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "Invalid mobile number")
    private String mobileNumber;
    private String address;
    @NotBlank @Size(min=8)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$",
             message = "Password must have uppercase, number, and special character")
    private String password;
    @NotBlank
    private String confirmPassword;
}
