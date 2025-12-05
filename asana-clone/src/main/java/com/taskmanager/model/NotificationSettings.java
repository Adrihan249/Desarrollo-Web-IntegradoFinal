package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad NotificationSettings (Configuración de Notificaciones)
 *
 * CUMPLE REQUERIMIENTO N°15: Configuración de notificaciones
 *
 * Permite a cada usuario personalizar qué notificaciones desea recibir
 */
@Entity
@Table(name = "notification_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario dueño de esta configuración
     * Relación OneToOne: cada usuario tiene una configuración
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ========================================================================
    // N°15: Configuración de Notificaciones de Tareas
    // ========================================================================

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyTaskAssigned = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyTaskStatusChanged = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyTaskDeadlineApproaching = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyTaskOverdue = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyTaskCompleted = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyTaskCommented = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyTaskAttachmentAdded = false;

    // ========================================================================
    // N°15: Configuración de Menciones y Comentarios
    // ========================================================================

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyMentioned = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyCommentReplies = true;

    // ========================================================================
    // N°15: Configuración de Proyectos
    // ========================================================================

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyProjectAdded = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyProjectStatusChanged = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyProjectDeadlineApproaching = true;

    // ========================================================================
    // N°15: Configuración de Subtareas
    // ========================================================================

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifySubtaskCompleted = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyAllSubtasksCompleted = true;

    // ========================================================================
    // N°15: Configuración General
    // ========================================================================

    /**
     * Habilitar/deshabilitar todas las notificaciones
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean notificationsEnabled = true;

    /**
     * Enviar resumen diario por email
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean dailyEmailSummary = false;

    /**
     * Enviar resumen semanal por email
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean weeklyEmailSummary = false;

    /**
     * Horas antes del deadline para notificar
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer hoursBeforeDeadline = 24;

    /**
     * Modo "No molestar" (silencia todas las notificaciones)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean doNotDisturb = false;

    /**
     * Hora de inicio del modo "No molestar"
     */
    @Column
    private Integer doNotDisturbStartHour; // 0-23

    /**
     * Hora de fin del modo "No molestar"
     */
    @Column
    private Integer doNotDisturbEndHour; // 0-23

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica si debe notificar según el tipo
     */
    public boolean shouldNotify(Notification.NotificationType type) {
        if (!notificationsEnabled || doNotDisturb) {
            return false;
        }

        return switch (type) {
            case TASK_ASSIGNED -> notifyTaskAssigned;
            case TASK_STATUS_CHANGED -> notifyTaskStatusChanged;
            case TASK_DEADLINE_APPROACHING -> notifyTaskDeadlineApproaching;
            case TASK_OVERDUE -> notifyTaskOverdue;
            case TASK_COMPLETED -> notifyTaskCompleted;
            case TASK_COMMENTED -> notifyTaskCommented;
            case TASK_ATTACHMENT_ADDED -> notifyTaskAttachmentAdded;
            case MENTIONED_IN_COMMENT -> notifyMentioned;
            case PROJECT_ADDED_AS_MEMBER -> notifyProjectAdded;
            case PROJECT_STATUS_CHANGED -> notifyProjectStatusChanged;
            case PROJECT_DEADLINE_APPROACHING -> notifyProjectDeadlineApproaching;
            case SUBTASK_COMPLETED -> notifySubtaskCompleted;
            case ALL_SUBTASKS_COMPLETED -> notifyAllSubtasksCompleted;
            default -> true; // Por defecto notifica
        };
    }

    /**
     * Verifica si está en horario "No molestar"
     */
    public boolean isInDoNotDisturbPeriod() {
        if (!doNotDisturb || doNotDisturbStartHour == null || doNotDisturbEndHour == null) {
            return false;
        }

        int currentHour = LocalDateTime.now().getHour();

        if (doNotDisturbStartHour < doNotDisturbEndHour) {
            // Mismo día: ej. 22:00 a 08:00 del día siguiente
            return currentHour >= doNotDisturbStartHour && currentHour < doNotDisturbEndHour;
        } else {
            // Cruza medianoche: ej. 08:00 a 22:00
            return currentHour >= doNotDisturbStartHour || currentHour < doNotDisturbEndHour;
        }
    }
}