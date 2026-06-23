package com.smartbank.service.impl;

import com.smartbank.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@SuppressWarnings("null")
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Async
    @Override
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Welcome to SmartBank Wallet 🎉";
        String body = buildEmailTemplate("Welcome to SmartBank, " + name + "!",
                "Your account has been successfully created. You can now enjoy seamless digital banking with SmartBank Wallet.",
                "Start Banking", "#", "green");
        sendEmail(to, subject, body);
    }

    @Async
    @Override
    public void sendTransferEmail(String to, String senderName, String receiverName, BigDecimal amount, BigDecimal newBalance) {
        String subject = "Money Transfer Successful ✅";
        String body = buildEmailTemplate("Money Transfer Successful",
                String.format("Dear %s,<br><br>You have successfully transferred <strong>₹%.2f</strong> to <strong>%s</strong>.<br><br>Your new balance: <strong>₹%.2f</strong>",
                        senderName, amount, receiverName, newBalance),
                "View Transaction", "#", "#1565C0");
        sendEmail(to, subject, body);
    }

    @Async
    @Override
    public void sendAddMoneyEmail(String to, String name, BigDecimal amount, BigDecimal newBalance) {
        String subject = "Money Added Successfully ✅";
        String body = buildEmailTemplate("Money Added to Wallet",
                String.format("Dear %s,<br><br><strong>₹%.2f</strong> has been successfully added to your SmartBank Wallet.<br><br>Current Balance: <strong>₹%.2f</strong>",
                        name, amount, newBalance),
                "View Wallet", "#", "#2E7D32");
        sendEmail(to, subject, body);
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        String subject = "Password Reset Request 🔐";
        String body = buildEmailTemplate("Password Reset Request",
                String.format("Dear %s,<br><br>We received a request to reset your password. Click the button below to proceed. This link expires in 1 hour.", name),
                "Reset Password", resetLink, "#C62828");
        sendEmail(to, subject, body);
    }

    @Async
    @Override
    public void sendPasswordChangedEmail(String to, String name) {
        String subject = "Password Changed Successfully 🔒";
        String body = buildEmailTemplate("Password Changed",
                String.format("Dear %s,<br><br>Your SmartBank Wallet password has been changed successfully. If you did not make this change, please contact support immediately.", name),
                "Login Now", "#", "#1565C0");
        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildEmailTemplate(String title, String content, String btnText, String btnLink, String color) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);">
                    <tr><td style="background:linear-gradient(135deg,#1565C0,#0D47A1);padding:30px;text-align:center;">
                      <h1 style="color:#fff;margin:0;font-size:28px;letter-spacing:1px;">🏦 SmartBank Wallet</h1>
                    </td></tr>
                    <tr><td style="padding:40px;">
                      <h2 style="color:#1565C0;margin-bottom:20px;">%s</h2>
                      <p style="color:#555;line-height:1.8;font-size:16px;">%s</p>
                      <div style="text-align:center;margin:30px 0;">
                        <a href="%s" style="background:%s;color:#fff;padding:14px 36px;text-decoration:none;border-radius:8px;font-weight:bold;font-size:16px;">%s</a>
                      </div>
                    </td></tr>
                    <tr><td style="background:#f8f9fa;padding:20px;text-align:center;">
                      <p style="color:#999;font-size:12px;margin:0;">© 2024 SmartBank Wallet. All rights reserved.</p>
                      <p style="color:#999;font-size:12px;margin:5px 0;">This is an automated email. Please do not reply.</p>
                    </td></tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(title, content, btnLink, color, btnText);
    }
}
