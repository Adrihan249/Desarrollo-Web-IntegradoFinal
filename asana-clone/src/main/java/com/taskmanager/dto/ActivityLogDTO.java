package com.taskmanager.dto;

import lombok.*;

import java.time.LocalDateTime;
/**
 * DTOs para ActivityLog (N°7)
 */
public class ActivityLogDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long projectId;
        private UserDTO.Summary user;
        private String activityType;
        private String entityType;
        private Long entityId;
        private String entityName;
        private String description;
        private String icon;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Timeline {
        private String date; // "2024-01-15"
        private java.util.List<Response> activities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long totalActivities;
        private Long tasksCreated;
        private Long tasksCompleted;
        private Long commentsAdded;
        private Long filesUploaded;
        private java.util.Map<String, Long> activitiesByType;
        private java.util.Map<String, Long> activitiesByUser;
    }
}
