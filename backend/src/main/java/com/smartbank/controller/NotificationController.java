package com.smartbank.controller;

import com.smartbank.dto.response.ApiResponse;
import com.smartbank.dto.response.NotificationResponse;
import com.smartbank.entity.Notification;
import com.smartbank.exception.ResourceNotFoundException;
import com.smartbank.repository.NotificationRepository;
import com.smartbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(Principal principal) {
        Long userId = getUserId(principal);
        List<NotificationResponse> list = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched", list));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(Principal principal) {
        Long userId = getUserId(principal);
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread count", Map.of("count", count)));
    }

    @Transactional
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id, Principal principal) {
        Long userId = getUserId(principal);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @Transactional
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Principal principal) {
        Long userId = getUserId(principal);
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok(ApiResponse.success("All marked as read", null));
    }

    private Long getUserId(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType().name())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
