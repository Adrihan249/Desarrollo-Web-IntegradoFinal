package com.taskmanager.mapper;

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import com.taskmanager.model.Process;
import org.mapstruct.*;

import java.util.List;

/**
 * ProcessMapper - Mapeo entre Process entities y DTOs
 *
 * CUMPLE REQUERIMIENTO N°5: Gestión de procesos
 */
@Mapper(
        componentModel = "spring",
        uses = {TaskMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProcessMapper {

    /**
     * N°5: Mapea CreateRequest a Process
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true) // Se setea en el servicio
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Process createRequestToProcess(ProcessDTO.CreateRequest request);

    /**
     * N°5: Mapea Process a Response
     * Incluye las tareas del proceso
     */
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "taskCount", expression = "java(process.getTasks() != null ? process.getTasks().size() : 0)")
    @Mapping(target = "isOverLimit", expression = "java(isProcessOverLimit(process))")
    ProcessDTO.Response processToResponse(Process process);

    /**
     * N°5: Mapea Process a Summary
     */
    @Mapping(target = "taskCount", expression = "java(process.getTasks() != null ? process.getTasks().size() : 0)")
    ProcessDTO.Summary processToSummary(Process process);

    /**
     * N°5: Actualiza Process desde UpdateRequest
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProcessFromDto(ProcessDTO.UpdateRequest request, @MappingTarget Process process);

    /**
     * Verifica si el proceso excede el límite WIP
     */
    default boolean isProcessOverLimit(Process process) {
        if (process.getTaskLimit() == null) {
            return false;
        }
        int taskCount = process.getTasks() != null ? process.getTasks().size() : 0;
        return taskCount > process.getTaskLimit();
    }
}
