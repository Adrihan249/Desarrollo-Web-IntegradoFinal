package com.taskmanager.mapper;

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import org.mapstruct.*;

import java.util.List;
/**
 * ===================================================================
 * TaskMapper - Mapeo entre Task entities y DTOs
 *
 * CUMPLE REQUERIMIENTOS:
 * - N°6: Estados de tareas
 * - N°18: Subtareas
 * ===================================================================
 */
@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TaskMapper {

    /**
     * N°6: Mapea CreateRequest a Task
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "process", ignore = true) // Se setea en el servicio
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "assignees", ignore = true)
    @Mapping(target = "parentTask", ignore = true)
    @Mapping(target = "subtasks", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "priority", source = "priority", qualifiedByName = "stringToPriority")
    Task createRequestToTask(TaskDTO.CreateRequest request);

    /**
     * N°6 + N°18: Mapea Task a Response
     * [CORREGIDO] Mapeo de Process a campos planos.
     */
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "processId", source = "process.id") // <-- NUEVO MAPPING
    @Mapping(target = "processName", source = "process.name") // <-- NUEVO MAPPING
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "priorityToString")
    @Mapping(target = "parentTaskId", source = "parentTask.id")
    @Mapping(target = "commentCount", expression = "java(task.getComments() != null ? task.getComments().size() : 0)")
    @Mapping(target = "attachmentCount", expression = "java(task.getAttachments() != null ? task.getAttachments().size() : 0)")
    @Mapping(target = "isOverdue", expression = "java(task.isOverdue())")
    TaskDTO.Response taskToResponse(Task task);

    /**
     * Mapea Task a Summary
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "priorityToString")
    @Mapping(target = "subtaskCount", expression = "java(task.getSubtasks() != null ? task.getSubtasks().size() : 0)")
    @Mapping(target = "isOverdue", expression = "java(task.isOverdue())")
    TaskDTO.Summary taskToSummary(Task task);

    /**
     * Lista de Tasks a lista de Summaries
     */
    List<TaskDTO.Summary> tasksToSummaries(List<Task> tasks);

    /**
     * N°6: Actualiza Task desde UpdateRequest
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "process", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "assignees", ignore = true)
    @Mapping(target = "parentTask", ignore = true)
    @Mapping(target = "subtasks", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "stringToPriority")
    void updateTaskFromDto(TaskDTO.UpdateRequest request, @MappingTarget Task task);

    /**
     * Conversión de enums a String
     */
    @Named("statusToString")
    default String statusToString(Task.TaskStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("priorityToString")
    default String priorityToString(Task.TaskPriority priority) {
        return priority != null ? priority.name() : null;
    }

    /**
     * Conversión de String a enums
     */
    @Named("stringToStatus")
    default Task.TaskStatus stringToStatus(String status) {
        if (status == null) return null;
        try {
            return Task.TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("stringToPriority")
    default Task.TaskPriority stringToPriority(String priority) {
        if (priority == null) return null;
        try {
            return Task.TaskPriority.valueOf(priority);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}