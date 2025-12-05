package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad ActivityLog (Registro de Actividad)
 *
 * CUMPLE REQUERIMIENTO N°7: Seguimiento de avances
 *
 * Registra todas las acciones realizadas en el proyecto.
 */
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_project_created", columnList = "project_id,created_at"),
        @Index(name = "idx_user_created", columnList = "user_id,created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Proyecto donde ocurrió la actividad
     */
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Usuario que realizó la acción
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Tipo de actividad
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityType activityType;

    /**
     * Entidad afectada
     */
    @Column(nullable = false, length = 50)
    private String entityType; // "TASK", "PROJECT", "COMMENT", etc.

    @Column(nullable = false)
    private Long entityId;

    /**
     * Nombre/título de la entidad (para mostrar sin consultar)
     */
    @Column(length = 200)
    private String entityName;

    /**
     * Descripción legible de la actividad
     */
    @Column(nullable = false, length = 500)
    private String description;

    /**
     * Datos antes del cambio (JSON)
     */
    @Column(length = 2000)
    private String previousValue;

    /**
     * Datos después del cambio (JSON)
     */
    @Column(length = 2000)
    private String newValue;

    /**
     * Metadatos adicionales (JSON)
     */
    @Column(length = 1000)
    private String metadata;

    /**
     * Dirección IP del usuario
     */
    @Column(length = 45)
    private String ipAddress;

    /**
     * User Agent del navegador
     */
    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Tipos de actividad para seguimiento (N°7)
     */
    public enum ActivityType {
        // --- Proyectos ---
        PROJECT_CREATED("creó el proyecto", "🎯"),
        PROJECT_UPDATED("actualizó el proyecto", "✏️"),
        PROJECT_ARCHIVED("archivó el proyecto", "📦"),
        PROJECT_STATUS_CHANGED("cambió el estado del proyecto", "🔄"),
        PROJECT_UNARCHIVED("desarchivó el proyecto", "📦"),

        // --- Procesos ---
        PROCESS_CREATED("creó el proceso", "➕"),
        PROCESS_UPDATED("actualizó el proceso", "✏️"),
        PROCESS_DELETED("eliminó el proceso", "🗑️"),
        PROCESS_REORDERED("reordenó los procesos", "↕️"),

        // --- Tareas ---
        TASK_CREATED("creó la tarea", "📝"),
        TASK_UPDATED("actualizó la tarea", "✏️"),
        TASK_DELETED("eliminó la tarea", "🗑️"),
        TASK_STATUS_CHANGED("cambió el estado de la tarea", "🔄"),
        TASK_PRIORITY_CHANGED("cambió la prioridad de la tarea", "⚡"),
        TASK_MOVED("movió la tarea", "➡️"),
        TASK_COMPLETED("completó la tarea", "✅"),

        // 🟢 CORRECCIÓN: Se añaden USER_ASSIGNED/UNASSIGNED para el TaskService
        USER_ASSIGNED("asignó a un usuario a", "👤"),      // Reemplaza TASK_ASSIGNED
        USER_UNASSIGNED("desasignó a un usuario de", "👤"), // Reemplaza TASK_UNASSIGNED

        // --- Subtareas ---
        SUBTASK_CREATED("creó la subtarea", "📝"),
        SUBTASK_COMPLETED("completó la subtarea", "✅"),

        // --- Comentarios ---
        COMMENT_ADDED("comentó en", "💬"),
        COMMENT_UPDATED("editó un comentario en", "✏️"),
        COMMENT_DELETED("eliminó un comentario en", "🗑️"),

        // --- Archivos ---
        ATTACHMENT_UPLOADED("subió un archivo a", "📎"),
        ATTACHMENT_DELETED("eliminó un archivo de", "🗑️"),

        // --- Miembros del Proyecto ---
        MEMBER_ADDED("agregó a un miembro al proyecto", "➕"),
        MEMBER_REMOVED("removió a un miembro del proyecto", "➖"),
        MEMBER_INVITED("invitó a un nuevo miembro","\uD83D\uDCE7"),

        // --- Chat ---
        CHAT_MESSAGE_SENT("envió un mensaje", "💬"),
        CHAT_MESSAGE_PINNED("fijó un mensaje", "📌"),

        // --- Sistema ---
        USER_JOINED("se unió al proyecto", "👋"),
        USER_LEFT("abandonó el proyecto", "👋");


        private final String actionText;
        private final String icon;

        ActivityType(String actionText, String icon) {
            this.actionText = actionText;
            this.icon = icon;
        }

        public String getActionText() {
            return actionText;
        }

        public String getIcon() {
            return icon;
        }
    }

    /**
     * Formatea la actividad como texto legible
     */
    public String getFormattedActivity() {
        return String.format("%s %s %s: %s",
                user.getFullName(),
                activityType.getActionText(),
                entityType.toLowerCase(),
                entityName);
    }
}