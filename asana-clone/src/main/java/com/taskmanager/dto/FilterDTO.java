package com.taskmanager.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
/**
 * DTOs para filtros avanzados (N°13)
 */
public class FilterDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskFilterRequest {
        // Filtros básicos
        private Set<String> statuses;
        private Set<String> priorities;
        private Set<Long> assigneeIds;
        private Set<String> tags;

        // Filtros de fecha
        private LocalDateTime dueDateFrom;
        private LocalDateTime dueDateTo;
        private LocalDateTime createdAtFrom;
        private LocalDateTime createdAtTo;

        // Filtros avanzados
        private Boolean overdue;
        private Boolean hasSubtasks;
        private Boolean isSubtask;
        private Integer minCompletionPercentage;
        private Integer maxCompletionPercentage;

        // Ordenamiento
        private String sortBy; // "dueDate", "priority", "createdAt", etc.
        private String sortDirection; // "ASC", "DESC"

        // Paginación
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectFilterRequest {
        private Set<String> statuses;
        private Boolean archived;
        private LocalDateTime deadlineFrom;
        private LocalDateTime deadlineTo;
        private Long createdById;
        private Integer minMemberCount;
        private Integer maxMemberCount;

        private String sortBy;
        private String sortDirection;
        private Integer page;
        private Integer size;
    }
}