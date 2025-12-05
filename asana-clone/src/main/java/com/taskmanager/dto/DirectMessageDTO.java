package com.taskmanager.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class DirectMessageDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendRequest {

        @NotNull(message = "El ID del destinatario es obligatorio")
        private Long receiverId;

        @NotBlank(message = "El contenido es obligatorio")
        @Size(min = 1, max = 2000, message = "El mensaje debe tener entre 1 y 2000 caracteres")
        private String content;

        private String type; // TEXT, FILE, IMAGE

        // Para archivos
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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String conversationId;
        private UserDTO.Summary sender;
        private UserDTO.Summary receiver;
        private String content;
        private String type;

        // Archivo adjunto
        private String attachmentUrl;
        private String attachmentName;
        private String attachmentMimeType;
        private Long attachmentSize;

        private Boolean isRead;
        private LocalDateTime readAt;

        private Boolean edited;
        private LocalDateTime editedAt;

        private Boolean deleted;

        private Map<String, Integer> reactionCounts;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationSummary {
        private String conversationId;
        private UserDTO.Summary otherUser;
        private Response lastMessage;
        private Long unreadCount;
        private LocalDateTime lastMessageAt;

        // Información de contexto
        private Set<String> sharedProjectNames;
        private Boolean isProjectMember;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockRequest {

        @NotNull(message = "El ID del usuario a bloquear es obligatorio")
        private Long blockedUserId;

        @Size(max = 500, message = "La razón no puede exceder 500 caracteres")
        private String reason;
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