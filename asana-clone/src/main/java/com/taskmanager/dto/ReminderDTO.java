package com.taskmanager.dto;

import com.taskmanager.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
// ===================================
// REMINDER DTOs
// ===================================

@Getter
@Setter
@NoArgsConstructor
public class ReminderDTO {

    // Response DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private ReminderType type;
        private Long referenceId;
        private String referenceType;
        private String title;
        private String message;
        private LocalDateTime reminderDate;
        private LocalDateTime snoozeUntil;
        private ReminderFrequency frequency;
        private Integer advanceMinutes;
        private ReminderStatus status;
        private LocalDateTime sentAt;
        private Boolean emailNotification;
        private Boolean inAppNotification;
        private Boolean pushNotification;
        private Boolean isOverdue;
        private String timeUntilReminder;
        private LocalDateTime createdAt;
    }

    // Create Request
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotNull(message = "Reminder type is required")
        private ReminderType type;

        private Long referenceId;
        private String referenceType;

        @NotBlank(message = "Title is required")
        @Size(max = 200)
        private String title;

        @Size(max = 1000)
        private String message;

        @NotNull(message = "Reminder date is required")
        @Future(message = "Reminder date must be in the future")
        private LocalDateTime reminderDate;

        @NotNull
        private ReminderFrequency frequency;

        private Integer advanceMinutes;

        private Boolean emailNotification;
        private Boolean inAppNotification;
        private Boolean pushNotification;
    }

    // Update Request
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @Size(max = 200)
        private String title;

        @Size(max = 1000)
        private String message;

        @Future
        private LocalDateTime reminderDate;

        private ReminderFrequency frequency;
        private Integer advanceMinutes;
        private Boolean emailNotification;
        private Boolean inAppNotification;
        private Boolean pushNotification;
    }

    // Snooze Request
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SnoozeRequest {
        @NotNull(message = "Minutes is required")
        @Min(value = 5, message = "Minimum snooze time is 5 minutes")
        @Max(value = 1440, message = "Maximum snooze time is 24 hours")
        private Integer minutes;
    }
}
