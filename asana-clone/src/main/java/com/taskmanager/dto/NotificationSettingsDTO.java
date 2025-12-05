package com.taskmanager.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
/**
 * DTOs para NotificationSettings (N°15)
 */
public class NotificationSettingsDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        // Tareas
        private Boolean notifyTaskAssigned;
        private Boolean notifyTaskStatusChanged;
        private Boolean notifyTaskDeadlineApproaching;
        private Boolean notifyTaskOverdue;
        private Boolean notifyTaskCompleted;
        private Boolean notifyTaskCommented;
        private Boolean notifyTaskAttachmentAdded;

        // Menciones
        private Boolean notifyMentioned;
        private Boolean notifyCommentReplies;

        // Proyectos
        private Boolean notifyProjectAdded;
        private Boolean notifyProjectStatusChanged;
        private Boolean notifyProjectDeadlineApproaching;

        // Subtareas
        private Boolean notifySubtaskCompleted;
        private Boolean notifyAllSubtasksCompleted;

        // General
        private Boolean notificationsEnabled;
        private Boolean dailyEmailSummary;
        private Boolean weeklyEmailSummary;
        private Integer hoursBeforeDeadline;
        private Boolean doNotDisturb;
        private Integer doNotDisturbStartHour;
        private Integer doNotDisturbEndHour;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        // Se pueden actualizar todos los campos individualmente
        private Boolean notifyTaskAssigned;
        private Boolean notifyTaskStatusChanged;
        private Boolean notifyTaskDeadlineApproaching;
        private Boolean notifyTaskOverdue;
        private Boolean notifyTaskCompleted;
        private Boolean notifyTaskCommented;
        private Boolean notifyTaskAttachmentAdded;
        private Boolean notifyMentioned;
        private Boolean notifyCommentReplies;
        private Boolean notifyProjectAdded;
        private Boolean notifyProjectStatusChanged;
        private Boolean notifyProjectDeadlineApproaching;
        private Boolean notifySubtaskCompleted;
        private Boolean notifyAllSubtasksCompleted;
        private Boolean notificationsEnabled;
        private Boolean dailyEmailSummary;
        private Boolean weeklyEmailSummary;

        @Min(value = 1, message = "Debe ser al menos 1 hora")
        @Max(value = 168, message = "No puede exceder 168 horas (1 semana)")
        private Integer hoursBeforeDeadline;

        private Boolean doNotDisturb;

        @Min(value = 0)
        @Max(value = 23)
        private Integer doNotDisturbStartHour;

        @Min(value = 0)
        @Max(value = 23)
        private Integer doNotDisturbEndHour;
    }
}
