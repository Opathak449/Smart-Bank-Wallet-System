package com.smartbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank @Size(min = 2, max = 50)
    private String lastName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid mobile number")
    private String mobileNumber;

    private String address;
}
