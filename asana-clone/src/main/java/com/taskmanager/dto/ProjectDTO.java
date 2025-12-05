package com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
// Se asume que Project y UserDTO existen en el paquete com.taskmanager.dto
// Se asume que el mapper y Collectors se importarán en el mapper class.

/**
 * DTOs para Project
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectDTO {

    // --- DTOs de Petición (Requests) ---

    /**
     * DTO para creación de proyectos (N°3)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {

        @NotBlank(message = "El nombre del proyecto es obligatorio")
        @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
        private String name;

        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        private String description;

        private String color; // Hexadecimal, ej: #3B82F6

        private LocalDateTime deadline;

        // IDs de usuarios a agregar como miembros iniciales (N°4)
        private Set<Long> memberIds;
    }
// 🔥 NUEVO DTO PARA INVITAR POR EMAIL
    /**
     * DTO para enviar una invitación de miembro por email (N°19)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InviteMemberRequest {
        @NotBlank(message = "El email del invitado es obligatorio")
        private String invitedEmail;
    }
    /**
     * DTO para actualización de proyectos
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
        private String name;

        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        private String description;

        private String color;

        private LocalDateTime deadline;

        private String status; // ACTIVE, ON_HOLD, COMPLETED, CANCELLED
    }

    /**
     * DTO para actualizar solo el estado de un proyecto.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdateRequest {
        // Se recomienda usar el Enum si es posible, pero String es funcional.
        private String status;
    }

    /**
     * DTO para agregar/remover miembros (N°4)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberRequest {
        private Long userId;
    }


    // --- DTOs de Respuesta (Responses) ---

    /**
     * DTO de respuesta completa del proyecto, incluyendo procesos con tareas.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String color;
        private String status;
        private LocalDateTime deadline;
        private Boolean archived;
        private UserDTO.Summary createdBy;
        private Set<UserDTO.Summary> members;

        // AGREGAR LA LISTA DE PROCESOS CON TAREAS
        private java.util.List<ProcessDTO.SummaryWithTasks> processes;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * DTO resumido para listados
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String name;
        private String description;
        private String color;
        private String status;
        private Integer memberCount;
        private LocalDateTime deadline;
        private LocalDateTime createdAt;
    }
}