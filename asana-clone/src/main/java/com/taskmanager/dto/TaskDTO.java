package com.taskmanager.dto;

import com.taskmanager.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;

/**
 * DTOs para Task
 *
 * CUMPLE REQUERIMIENTOS:
 * - N°6: Estados de tareas
 * - N°18: Subtareas
 * - N°10: Comentarios (referencias)
 * - N°11: Adjuntos (referencias)
 * - N°17: Fecha límite
 */


/**
 * DTOs para Task
 */
public class TaskDTO {

    // =========================================================================
    // NUEVO DTO ANIDADO PARA RECIBIR SUBTAREAS EN LA CREACIÓN
    // =========================================================================

    /**
     * DTO específico para los datos que se reciben de una subtarea al crear la tarea padre.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubtaskRequest {

        @NotBlank(message = "El título de la subtarea es obligatorio")
        @Size(min = 3, max = 200, message = "El título debe tener entre 3 y 200 caracteres")
        private String title;

        // IDs de usuarios asignados a esta subtarea
        private Set<Long> assigneeIds;

        // Fecha límite específica de esta subtarea
        private LocalDateTime dueDate;
    }

    // =========================================================================


    /**
     * DTO para crear una tarea (N°6) - MODIFICADO para incluir Subtasks
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {

        @NotBlank(message = "El título es obligatorio")
        @Size(min = 3, max = 200, message = "El título debe tener entre 3 y 200 caracteres")
        private String title;

        @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
        private String description;

        // ID del proceso (columna Kanban) donde se creará la tarea
        private Long processId;

        // IDs de usuarios asignados a la Tarea Principal
        private Set<Long> assigneeIds;

        private String priority; // LOW, MEDIUM, HIGH, URGENT

        // N°17: Fecha límite
        private LocalDateTime dueDate;

        private LocalDateTime startDate;

        // Etiquetas
        private Set<String> tags;

        private Integer estimatedHours;

        // N°18: ID de tarea padre (para crear subtarea manualmente)
        private Long parentTaskId;

        // N°18: LISTA DE SUBTAREAS ANIDADAS (¡El campo clave añadido!)
        private List<SubtaskRequest> subtasks;
    }

    /**
     * DTO para actualizar una tarea
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @Size(min = 3, max = 200, message = "El título debe tener entre 3 y 200 caracteres")
        private String title;

        @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
        private String description;

        private String status; // TODO, IN_PROGRESS, IN_REVIEW, BLOCKED, DONE, CANCELLED

        private String priority;

        private LocalDateTime dueDate;

        private LocalDateTime startDate;

        private Set<String> tags;

        private Integer estimatedHours;

        private Integer actualHours;

        @Min(value = 0, message = "El porcentaje debe ser mínimo 0")
        @Max(value = 100, message = "El porcentaje debe ser máximo 100")
        private Integer completionPercentage;
    }

    /**
     * DTO de respuesta completa de tarea
     * [CORREGIDO] Reemplazo de ProcessDTO.Summary por campos planos para evitar recursividad.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String description;

        // Información del proceso (Corregido para evitar el ciclo de recursividad)
        private Long processId;
        private String processName;

        // Información del proyecto
        private Long projectId;
        private String projectName;

        // Creador
        private UserDTO.Summary createdBy;

        // Asignados
        private Set<UserDTO.Summary> assignees;

        private String status;
        private String priority;

        private LocalDateTime dueDate;
        private LocalDateTime startDate;
        private LocalDateTime completedAt;

        // N°18: Tarea padre y subtareas
        private Long parentTaskId;
        private List<Summary> subtasks;

        // N°10: Contador de comentarios
        private Integer commentCount;

        // N°11: Contador de adjuntos
        private Integer attachmentCount;

        private Integer position;
        private Set<String> tags;
        private Integer estimatedHours;
        private Integer actualHours;
        private Integer completionPercentage;

        private Boolean isOverdue;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * DTO resumido para listados y subtareas
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String title;
        private String status;
        private String priority;
        private LocalDateTime dueDate;
        private Integer completionPercentage;
        private Set<UserDTO.Summary> assignees;
        private Integer subtaskCount;
        private Boolean isOverdue;
    }

    /**
     * DTO para mover tarea entre procesos (N°5)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoveRequest {
        private Long targetProcessId;
        private Integer position; // Posición en la nueva columna
    }

    /**
     * DTO para asignar/desasignar usuarios
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignRequest {
        private Long userId;
    }

    /**
     * Simple response para recordatorios
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimpleResponse {
        private Long id;
        private String title;
        private LocalDateTime dueDate;
        private Task.TaskStatus status;
    }
}