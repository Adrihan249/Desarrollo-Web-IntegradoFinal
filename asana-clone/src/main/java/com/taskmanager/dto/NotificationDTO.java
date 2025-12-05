package com.taskmanager.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * DTOs para Notification (N°8)
 */
public class NotificationDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private UserDTO.Summary actor;
        private String type;
        private String title;
        private String message;
        private String entityType;
        private Long entityId;
        private String actionUrl;
        private String icon;
        private String priority;
        private Boolean read;
        private LocalDateTime readAt;
        private Boolean archived;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String type;
        private String title;
        private String icon;
        private Boolean read;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarkReadRequest {
        private Set<Long> notificationIds;
    }
}