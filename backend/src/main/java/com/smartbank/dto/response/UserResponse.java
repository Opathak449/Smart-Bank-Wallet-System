// UserResponse.java
package com.smartbank.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String mobileNumber;
    private String address;
    private String profileImage;
    private String status;
    private List<String> roles;
    private LocalDateTime createdAt;
    private String walletAccountNumber;
    private java.math.BigDecimal walletBalance;
    private String walletStatus;
}
