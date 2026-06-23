package com.smartbank.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "notifications")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private NotificationType type = NotificationType.INFO;

    @Builder.Default
    @Column(name = "is_read")
    private boolean read = false;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum NotificationType { INFO, SUCCESS, WARNING, ERROR }
}
