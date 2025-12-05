package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad ChatMessage (Mensaje de Chat)
 *
 * CUMPLE REQUERIMIENTO N°14: Chat del proyecto
 *
 * Sistema de chat en tiempo real para comunicación del equipo:
 * - Chat general del proyecto
 * - Mensajes con menciones
 * - Reacciones a mensajes
 * - Archivos compartidos
 */
@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Proyecto al que pertenece el mensaje
     */
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Usuario que envió el mensaje
     */
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Contenido del mensaje
     */
    @Column(nullable = false, length = 2000)
    private String content;

    /**
     * Tipo de mensaje
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    /**
     * Mensaje padre (para respuestas/hilos)
     */
    @ManyToOne
    @JoinColumn(name = "parent_message_id")
    private ChatMessage parentMessage;

    /**
     * Usuarios mencionados en el mensaje
     */
    @ElementCollection
    @CollectionTable(
            name = "chat_message_mentions",
            joinColumns = @JoinColumn(name = "message_id")
    )
    @Column(name = "user_id")
    @Builder.Default
    private Set<Long> mentionedUserIds = new HashSet<>();

    /**
     * Archivo adjunto (si el mensaje es de tipo FILE)
     */
    @Column(length = 500)
    private String attachmentUrl;

    @Column(length = 255)
    private String attachmentName;

    @Column(length = 100)
    private String attachmentMimeType;

    @Column
    private Long attachmentSize;

    /**
     * Indica si el mensaje fue editado
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean edited = false;

    @Column
    private LocalDateTime editedAt;

    /**
     * Indica si el mensaje fue eliminado (soft delete)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * Reacciones al mensaje
     * Formato: emoji -> Set de user IDs
     */
    @ElementCollection
    @CollectionTable(
            name = "chat_message_reactions",
            joinColumns = @JoinColumn(name = "message_id")
    )
    @MapKeyColumn(name = "emoji")
    @Column(name = "user_id")
    @Builder.Default
    private java.util.Map<String, Set<Long>> reactions = new java.util.HashMap<>();

    /**
     * Número de respuestas a este mensaje
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer replyCount = 0;

    /**
     * Usuarios que han leído el mensaje
     */
    @ElementCollection
    @CollectionTable(
            name = "chat_message_read_by",
            joinColumns = @JoinColumn(name = "message_id")
    )
    @Column(name = "user_id")
    @Builder.Default
    private Set<Long> readByUserIds = new HashSet<>();

    /**
     * Fijado en el chat (mensajes importantes)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean pinned = false;

    @Column
    private LocalDateTime pinnedAt;

    @ManyToOne
    @JoinColumn(name = "pinned_by_id")
    private User pinnedBy;

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
     * Tipos de mensaje
     */
    public enum MessageType {
        TEXT,           // Mensaje de texto normal
        FILE,           // Archivo compartido
        SYSTEM,         // Mensaje del sistema (ej: "Juan se unió al proyecto")
        TASK_LINK,      // Enlace a una tarea
        CODE,           // Código formateado
        ANNOUNCEMENT    // Anuncio importante
    }

    /**
     * Marca el mensaje como editado
     */
    public void markAsEdited() {
        this.edited = true;
        this.editedAt = LocalDateTime.now();
    }

    /**
     * Elimina el mensaje (soft delete)
     */
    public void softDelete() {
        this.deleted = true;
        this.content = "[Mensaje eliminado]";
    }

    /**
     * Agrega una reacción al mensaje
     */
    public void addReaction(String emoji, Long userId) {
        reactions.computeIfAbsent(emoji, k -> new HashSet<>()).add(userId);
    }

    /**
     * Remueve una reacción del mensaje
     */
    public void removeReaction(String emoji, Long userId) {
        Set<Long> users = reactions.get(emoji);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                reactions.remove(emoji);
            }
        }
    }

    /**
     * Marca el mensaje como leído por un usuario
     */
    public void markAsReadBy(Long userId) {
        readByUserIds.add(userId);
    }

    /**
     * Fija el mensaje en el chat
     */
    public void pin(User user) {
        this.pinned = true;
        this.pinnedAt = LocalDateTime.now();
        this.pinnedBy = user;
    }

    /**
     * Desfija el mensaje
     */
    public void unpin() {
        this.pinned = false;
        this.pinnedAt = null;
        this.pinnedBy = null;
    }

    /**
     * Incrementa el contador de respuestas
     */
    public void incrementReplyCount() {
        this.replyCount++;
    }

    /**
     * Verifica si es una respuesta
     */
    public boolean isReply() {
        return this.parentMessage != null;
    }
}