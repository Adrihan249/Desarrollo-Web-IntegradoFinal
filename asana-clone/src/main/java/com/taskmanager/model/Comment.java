package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad Comment (Comentario)
 *
 * CUMPLE REQUERIMIENTO N°10: Comentarios en tareas
 *
 * Los usuarios pueden comentar en las tareas para:
 * - Discutir detalles de implementación
 * - Hacer preguntas
 * - Proporcionar actualizaciones de progreso
 * - Mencionar a otros usuarios
 * - Registrar historial de cambios
 */
@Entity
@Table(name = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tarea a la que pertenece el comentario
     */
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude // Evita recursión en toString()
    private Task task;

    /**
     * Usuario que escribió el comentario
     */
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Contenido del comentario
     * Soporta texto plano o Markdown
     */
    @Column(nullable = false, length = 5000)
    private String content;

    /**
     * Comentario padre (para respuestas/hilos)
     * Permite crear conversaciones anidadas
     */
    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    /**
     * Indica si el comentario fue editado
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean edited = false;

    /**
     * Fecha de edición (si fue editado)
     */
    @Column
    private LocalDateTime editedAt;

    /**
     * Indica si el comentario fue eliminado (soft delete)
     * Los comentarios eliminados se ocultan pero mantienen la estructura de hilos
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * Menciones a usuarios en el comentario
     * Formato: @usuario
     * Se almacenan los IDs de usuarios mencionados
     */
    @ElementCollection
    @CollectionTable(
            name = "comment_mentions",
            joinColumns = @JoinColumn(name = "comment_id")
    )
    @Column(name = "user_id")
    @Builder.Default
    private java.util.Set<Long> mentionedUserIds = new java.util.HashSet<>();

    @Column(nullable = false, updatable = false)
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
     * Marca el comentario como editado
     */
    public void markAsEdited() {
        this.edited = true;
        this.editedAt = LocalDateTime.now();
    }

    /**
     * Elimina el comentario (soft delete)
     */
    public void softDelete() {
        this.deleted = true;
        this.content = "[Comentario eliminado]";
    }

    /**
     * Verifica si es una respuesta a otro comentario
     */
    public boolean isReply() {
        return this.parentComment != null;
    }
}