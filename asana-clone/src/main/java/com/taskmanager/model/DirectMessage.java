package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad DirectMessage (Mensaje Directo)
 *
 * Sistema de chat privado 1-a-1 entre usuarios
 */
@Entity
@Table(name = "direct_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID de la conversación (mismo para ambos usuarios)
     */
    @Column(nullable = false, length = 100)
    private String conversationId;

    /**
     * Usuario que envía el mensaje
     */
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Usuario que recibe el mensaje
     */
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

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
     * Archivo adjunto
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
     * Estado del mensaje
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column
    private LocalDateTime readAt;

    /**
     * Editado
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean edited = false;

    @Column
    private LocalDateTime editedAt;

    /**
     * Eliminado (soft delete)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * Reacciones
     */
    @ElementCollection
    @CollectionTable(
            name = "direct_message_reactions",
            joinColumns = @JoinColumn(name = "message_id")
    )
    @MapKeyColumn(name = "emoji")
    @Column(name = "user_id")
    @Builder.Default
    private java.util.Map<String, Set<Long>> reactions = new java.util.HashMap<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Generar conversationId si no existe
        if (conversationId == null && sender != null && receiver != null) {
            conversationId = generateConversationId(sender.getId(), receiver.getId());
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum MessageType {
        TEXT,
        FILE,
        IMAGE,
        SYSTEM
    }

    /**
     * Genera ID único para la conversación (independiente del orden)
     */
    public static String generateConversationId(Long userId1, Long userId2) {
        long min = Math.min(userId1, userId2);
        long max = Math.max(userId1, userId2);
        return "conv_" + min + "_" + max;
    }

    public void markAsEdited() {
        this.edited = true;
        this.editedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deleted = true;
        this.content = "[Mensaje eliminado]";
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void addReaction(String emoji, Long userId) {
        reactions.computeIfAbsent(emoji, k -> new HashSet<>()).add(userId);
    }

    public void removeReaction(String emoji, Long userId) {
        Set<Long> users = reactions.get(emoji);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                reactions.remove(emoji);
            }
        }
    }
}