package com.taskmanager.dto;

import com.taskmanager.model.Invitation;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTOs para la Entidad Invitation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InvitationDTO {

    // --- DTOs de Respuesta (Responses) ---

    /**
     * DTO de respuesta para una invitación, contiene la información necesaria
     * para que el frontend pueda mostrarla y actuar sobre ella.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id; // El ID de la invitación es CRUCIAL para responder
        private String invitedEmail;
        private Invitation.InvitationStatus status;

        // Información del remitente
        private UserDTO.Summary sender;

        // Información resumida del proyecto
        private ProjectDTO.Summary project;

        private LocalDateTime sentAt;
        private LocalDateTime respondedAt;
    }

    /**
     * DTO de respuesta resumido para listas anidadas si fuera necesario
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String projectName;
        private String senderName;
    }
}