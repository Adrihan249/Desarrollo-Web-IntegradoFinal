package com.taskmanager.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
/**
 * DTOs para ChatMessage (N°14)
 */
public class ChatMessageDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendRequest {

        @NotBlank(message = "El contenido es obligatorio")
        @Size(min = 1, max = 2000, message = "El mensaje debe tener entre 1 y 2000 caracteres")
        private String content;

        private String type; // TEXT, FILE, CODE, ANNOUNCEMENT

        private Long parentMessageId; // Para respuestas

        private Set<Long> mentionedUserIds;

        // Para mensajes con archivo
        private String attachmentUrl;
        private String attachmentName;
        private String attachmentMimeType;
        private Long attachmentSize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @NotBlank(message = "El contenido es obligatorio")
        @Size(min = 1, max = 2000, message = "El mensaje debe tener entre 1 y 2000 caracteres")
        private String content;

        private Set<Long> mentionedUserIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long projectId;
        private UserDTO.Summary sender;
        private String content;
        private String type;
        private Long parentMessageId;
        private Set<UserDTO.Summary> mentionedUsers;

        // Archivo adjunto
        private String attachmentUrl;
        private String attachmentName;
        private String attachmentMimeType;
        private Long attachmentSize;

        private Boolean edited;
        private LocalDateTime editedAt;
        private Boolean deleted;

        // Reacciones: emoji -> cantidad
        private Map<String, Integer> reactionCounts;

        private Integer replyCount;
        private Set<Long> readByUserIds;

        private Boolean pinned;
        private LocalDateTime pinnedAt;
        private UserDTO.Summary pinnedBy;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionRequest {

        @NotBlank(message = "El emoji es obligatorio")
        @Size(max = 10)
        private String emoji;
    }
}
