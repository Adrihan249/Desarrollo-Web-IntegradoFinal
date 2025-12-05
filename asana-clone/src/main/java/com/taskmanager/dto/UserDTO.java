package com.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTOs para User
 *
 * Los DTOs (Data Transfer Objects) se usan para:
 * 1. Separar la capa de presentación de la capa de dominio
 * 2. Controlar qué datos se exponen al cliente
 * 3. Validar datos de entrada
 * 4. Evitar exponer entidades JPA directamente
 */
public class UserDTO {

    /**
     * DTO para registro de nuevos usuarios (N°1)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email inválido")
        private String email;

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        private String firstName;

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        private String lastName;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        private String password;

        private String phoneNumber;
    }

    /**
     * DTO para login de usuarios (N°1)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email inválido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    /**
     * DTO de respuesta con información del usuario autenticado
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String bio;
        private String avatarUrl;
        private String phoneNumber;
        private Set<String> roles;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * DTO para actualización de perfil (N°16)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        private String firstName;

        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        private String lastName;

        @Size(max = 500, message = "La biografía no puede exceder 500 caracteres")
        private String bio;

        private String avatarUrl;

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        private String phoneNumber;
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
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String avatarUrl;
    }

    /**
     * DTO para cambio de contraseña
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {

        @NotBlank(message = "La contraseña actual es obligatoria")
        private String currentPassword;

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
        private String newPassword;
    }
}