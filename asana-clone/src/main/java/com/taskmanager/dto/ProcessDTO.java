package com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para Process (Columnas Kanban)
 *
 * CUMPLE REQUERIMIENTO N°5: Gestión de procesos
 *
 * Un proceso representa una columna en el tablero Kanban
 * Ejemplos: "Por Hacer", "En Progreso", "Completado"
 */
public class ProcessDTO {

    /**
     * DTO para crear un proceso
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        private String name;

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        private String description;

        private String color; // Hexadecimal

        @Min(value = 0, message = "La posición debe ser mayor o igual a 0")
        private Integer position;

        // Límite WIP (Work In Progress)
        @Min(value = 1, message = "El límite debe ser al menos 1")
        private Integer taskLimit;

        // Indica si este proceso marca tareas como completadas
        private Boolean isCompleted;
    }

    /**
     * DTO para actualizar un proceso
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        private String name;

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        private String description;

        private String color;

        @Min(value = 0, message = "La posición debe ser mayor o igual a 0")
        private Integer position;

        @Min(value = 1, message = "El límite debe ser al menos 1")
        private Integer taskLimit;

        private Boolean isCompleted;
    }

    /**
     * DTO de respuesta completa de proceso
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
        private Integer position;
        private Integer taskLimit;
        private Boolean isCompleted;

        // Proyecto al que pertenece
        private Long projectId;

        // Tareas en este proceso
        private List<TaskDTO.Summary> tasks;

        // Estadísticas
        private Integer taskCount;
        private Boolean isOverLimit; // Si excede el WIP limit

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
        private String color;
        private Integer position;
        private Integer taskCount;
        private Integer taskLimit;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryWithTasks {
        private Long id;
        private String name;
        private String color;
        private Boolean isCompleted;
        private List<TaskDTO.Summary> tasks;
    }
    /**
     * DTO para reordenar procesos
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReorderRequest {
        private Long processId;
        private Integer newPosition;
    }
}