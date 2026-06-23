// ============================================================
// EmailService.java (Interface)
// ============================================================
package com.smartbank.service;

import java.math.BigDecimal;

public interface EmailService {
    void sendWelcomeEmail(String to, String name);
    void sendTransferEmail(String to, String senderName, String receiverName, BigDecimal amount, BigDecimal newBalance);
    void sendAddMoneyEmail(String to, String name, BigDecimal amount, BigDecimal newBalance);
    void sendPasswordResetEmail(String to, String name, String resetLink);
    void sendPasswordChangedEmail(String to, String name);
}
