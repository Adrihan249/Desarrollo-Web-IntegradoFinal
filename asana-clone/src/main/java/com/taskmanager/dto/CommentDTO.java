
package com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class CommentDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "El contenido del comentario es obligatorio")
        @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String content;
        private UserDTO.Summary user;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}