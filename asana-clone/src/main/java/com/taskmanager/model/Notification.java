package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad Notification (Notificación)
 *
 * CUMPLE REQUERIMIENTO N°8: Notificaciones internas
 *
 * Sistema de notificaciones para informar a los usuarios sobre:
 * - Asignación a tareas
 * - Menciones en comentarios
 * - Cambios en tareas que siguen
 * - Plazos próximos
 * - Actualizaciones en proyectos
 */
@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario destinatario de la notificación
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Usuario que generó la acción (puede ser null para notificaciones del sistema)
     */
    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    /**
     * Tipo de notificación
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    /**
     * Título de la notificación
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Mensaje descriptivo
     */
    @Column(nullable = false, length = 500)
    private String message;

    /**
     * Entidad relacionada (Task, Project, Comment, etc.)
     */
    @Column(length = 50)
    private String entityType; // "TASK", "PROJECT", "COMMENT"

    @Column
    private Long entityId;

    /**
     * URL de acción (para navegar al hacer click)
     */
    @Column(length = 500)
    private String actionUrl;

    /**
     * Icono o emoji para la notificación
     */
    @Column(length = 50)
    private String icon;

    /**
     * Prioridad de la notificación
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    /**
     * Estado de lectura
     */
    // Después
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean read = false;
    /**
     * Fecha de lectura
     */
    @Column
    private LocalDateTime readAt;

    /**
     * Archivada (oculta pero no eliminada)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Tipos de notificación
     */
    public enum NotificationType {
        // Tareas
        TASK_ASSIGNED("Te asignaron una tarea", "📋"),
        TASK_UNASSIGNED("Te removieron de una tarea", "📋"),
        TASK_STATUS_CHANGED("Estado de tarea actualizado", "🔄"),
        TASK_DEADLINE_APPROACHING("Tarea próxima a vencer", "⏰"),
        TASK_OVERDUE("Tarea vencida", "⚠️"),
        TASK_COMPLETED("Tarea completada", "✅"),
        TASK_COMMENTED("Nuevo comentario en tarea", "💬"),
        TASK_ATTACHMENT_ADDED("Nuevo archivo adjunto", "📎"),

        // 🔥 AÑADIR TIPOS DE INVITACIÓN
        PROJECT_INVITATION_RECEIVED("Invitación a Proyecto", "📧"), // Nuevo tipo para el invitado
        MEMBER_JOINED("Nuevo miembro se unió", "🧑‍🤝‍🧑"),              // Usado cuando la invitación es aceptada
        MEMBER_INVITE_REJECTED("Invitación rechazada", "😥"),   // Usado cuando la invitación es rechazada
        // Menciones
        MENTIONED_IN_COMMENT("Te mencionaron en un comentario", "👤"),

        // Proyectos
        PROJECT_ADDED_AS_MEMBER("Te agregaron a un proyecto", "🎯"),
        PROJECT_REMOVED_AS_MEMBER("Te removieron de un proyecto", "🎯"),
        PROJECT_STATUS_CHANGED("Estado del proyecto cambió", "🔄"),
        PROJECT_DEADLINE_APPROACHING("Proyecto próximo a vencer", "⏰"),
        // ...
        // Proyectos
        PROJECT_CREATED("Nuevo proyecto creado", "🎉"), // << Añadir este
        // ...
        // Subtareas
        SUBTASK_COMPLETED("Subtarea completada", "✅"),
        ALL_SUBTASKS_COMPLETED("Todas las subtareas completadas", "🎉"),

        // Sistema
        SYSTEM_ANNOUNCEMENT("Anuncio del sistema", "📢"),
        SYSTEM_MAINTENANCE("Mantenimiento programado", "🔧"),

        // Sprint 4 - Suscripciones
        SUBSCRIPTION_RENEWAL("Renovación de suscripción", "💳"),
        SUBSCRIPTION_TRIAL_ENDING("Período de prueba terminando", "⏰"),
        SUBSCRIPTION_EXPIRED("Suscripción expirada", "❌"),
        SUBSCRIPTION_CHANGED("Plan de suscripción cambiado", "🔄"),
        SUBSCRIPTION_CANCELLED("Suscripción cancelada", "🚫"),
        PAYMENT_SUCCESSFUL("Pago procesado exitosamente", "✅"),
        PAYMENT_FAILED("Pago fallido", "❌"),

        // Sprint 4 - Recordatorios y Exportación
        REMINDER("Recordatorio", "🔔"),
        EXPORT_READY("Exportación lista", "📥"),
        EXPORT_FAILED("Exportación fallida", "❌"),
        DIRECT_MESSAGE("Te llego un mensaje ", "🚫");
        private final String defaultTitle;
        private final String defaultIcon;

        NotificationType(String defaultTitle, String defaultIcon) {
            this.defaultTitle = defaultTitle;
            this.defaultIcon = defaultIcon;
        }

        public String getDefaultTitle() {
            return defaultTitle;
        }

        public String getDefaultIcon() {
            return defaultIcon;
        }
    }

    /**
     * Prioridades de notificación
     */
    public enum NotificationPriority {
        LOW,      // Informativa
        NORMAL,   // Estándar
        HIGH,     // Importante
        URGENT    // Requiere atención inmediata
    }

    /**
     * Marca la notificación como leída
     */
    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Archiva la notificación
     */
    public void archive() {
        this.archived = true;
    }
}