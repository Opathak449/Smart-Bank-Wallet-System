package com.smartbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CreateUserRequest {
    @NotBlank @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank @Size(min = 2, max = 50)
    private String lastName;

    @NotBlank @Size(min = 3, max = 50)
    private String username;

    @NotBlank @Email
    private String email;

    @NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "Invalid mobile number")
    private String mobileNumber;

    private String address;

    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private List<String> roles;

    private String status;
}
