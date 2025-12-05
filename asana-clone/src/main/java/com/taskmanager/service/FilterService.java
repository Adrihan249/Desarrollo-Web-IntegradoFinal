package com.taskmanager.service;

// ============================================================================
// FilterService - Servicio de Filtros Avanzados (N°13)
// ============================================================================
import com.taskmanager.dto.FilterDTO;
import com.taskmanager.dto.ProjectDTO;
import com.taskmanager.dto.TaskDTO;
import com.taskmanager.mapper.ProjectMapper;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.model.Project;
import com.taskmanager.model.Task;
import com.taskmanager.Repositorios.ProjectRepository;
import com.taskmanager.Repositorios.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Filtros Avanzados
 *
 * CUMPLE REQUERIMIENTO N°13: Filtros y búsqueda avanzada
 *
 * Permite filtrar tareas y proyectos con múltiples criterios combinados
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FilterService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;
    private final ProjectMapper projectMapper;

    /**
     * N°13: Filtra tareas con múltiples criterios
     */
    public Page<TaskDTO.Response> filterTasks(
            Long projectId,
            FilterDTO.TaskFilterRequest filter,
            Long userId) {
        log.debug("Filtering tasks for project {} with criteria: {}", projectId, filter);

        // Obtiene todas las tareas del proyecto
        List<Task> allTasks = taskRepository.findByProjectId(projectId);

        // Aplica filtros
        List<Task> filtered = allTasks.stream()
                .filter(task -> applyTaskFilters(task, filter))
                .collect(Collectors.toList());

        // Ordena
        List<Task> sorted = sortTasks(filtered, filter.getSortBy(), filter.getSortDirection());

        // Pagina
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;

        int start = page * size;
        int end = Math.min(start + size, sorted.size());

        List<Task> paginated = start < sorted.size()
                ? sorted.subList(start, end)
                : List.of();

        // Convierte a DTOs
        List<TaskDTO.Response> dtos = paginated.stream()
                .map(taskMapper::taskToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, PageRequest.of(page, size), sorted.size());
    }

    /**
     * Aplica filtros a una tarea
     */
    private boolean applyTaskFilters(Task task, FilterDTO.TaskFilterRequest filter) {
        // Filtro por estados
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            if (!filter.getStatuses().contains(task.getStatus().name())) {
                return false;
            }
        }

        // Filtro por prioridades
        if (filter.getPriorities() != null && !filter.getPriorities().isEmpty()) {
            if (!filter.getPriorities().contains(task.getPriority().name())) {
                return false;
            }
        }

        // Filtro por asignados
        if (filter.getAssigneeIds() != null && !filter.getAssigneeIds().isEmpty()) {
            boolean hasAssignee = task.getAssignees().stream()
                    .anyMatch(assignee -> filter.getAssigneeIds().contains(assignee.getId()));
            if (!hasAssignee) {
                return false;
            }
        }

        // Filtro por etiquetas
        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            if (task.getTags() == null || !task.getTags().containsAll(filter.getTags())) {
                return false;
            }
        }

        // Filtro por fecha límite
        if (filter.getDueDateFrom() != null &&
                (task.getDueDate() == null || task.getDueDate().isBefore(filter.getDueDateFrom()))) {
            return false;
        }

        if (filter.getDueDateTo() != null &&
                (task.getDueDate() == null || task.getDueDate().isAfter(filter.getDueDateTo()))) {
            return false;
        }

        // Filtro por fecha de creación
        if (filter.getCreatedAtFrom() != null &&
                task.getCreatedAt().isBefore(filter.getCreatedAtFrom())) {
            return false;
        }

        if (filter.getCreatedAtTo() != null &&
                task.getCreatedAt().isAfter(filter.getCreatedAtTo())) {
            return false;
        }

        // Filtro por vencidas
        if (filter.getOverdue() != null && filter.getOverdue()) {
            if (!task.isOverdue()) {
                return false;
            }
        }

        // Filtro por tiene subtareas
        if (filter.getHasSubtasks() != null && filter.getHasSubtasks()) {
            if (task.getSubtasks() == null || task.getSubtasks().isEmpty()) {
                return false;
            }
        }

        // Filtro por es subtarea
        if (filter.getIsSubtask() != null && filter.getIsSubtask()) {
            if (!task.isSubtask()) {
                return false;
            }
        }

        // Filtro por porcentaje de completado
        if (filter.getMinCompletionPercentage() != null) {
            if (task.getCompletionPercentage() < filter.getMinCompletionPercentage()) {
                return false;
            }
        }

        if (filter.getMaxCompletionPercentage() != null) {
            if (task.getCompletionPercentage() > filter.getMaxCompletionPercentage()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Ordena tareas según criterio
     */
    private List<Task> sortTasks(List<Task> tasks, String sortBy, String sortDirection) {
        if (sortBy == null) {
            return tasks;
        }

        boolean ascending = "ASC".equalsIgnoreCase(sortDirection);

        return tasks.stream()
                .sorted((t1, t2) -> {
                    int comparison = switch (sortBy) {
                        case "dueDate" -> compareDates(t1.getDueDate(), t2.getDueDate());
                        case "priority" -> t1.getPriority().compareTo(t2.getPriority());
                        case "createdAt" -> t1.getCreatedAt().compareTo(t2.getCreatedAt());
                        case "completionPercentage" ->
                                t1.getCompletionPercentage().compareTo(t2.getCompletionPercentage());
                        default -> t1.getId().compareTo(t2.getId());
                    };
                    return ascending ? comparison : -comparison;
                })
                .collect(Collectors.toList());
    }

    /**
     * Compara fechas manejando nulls
     */
    private int compareDates(java.time.LocalDateTime d1, java.time.LocalDateTime d2) {
        if (d1 == null && d2 == null) return 0;
        if (d1 == null) return 1;
        if (d2 == null) return -1;
        return d1.compareTo(d2);
    }
}