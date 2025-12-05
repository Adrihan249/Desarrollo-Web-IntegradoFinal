package com.taskmanager.mapper;

import com.taskmanager.dto.ProjectDTO;
import com.taskmanager.model.Project;
import org.mapstruct.*;

/**
 * ProjectMapper - Mapeo entre Project entities y DTOs
 *
 * CUMPLE REQUERIMIENTOS:
 * - N°3: Creación de proyectos
 * - N°4: Asignación de colaboradores
 */
@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class}, // Usa UserMapper para mapear miembros
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProjectMapper {

    /**
     * Mapea CreateRequest a Project
     * Usado en N°3: Creación de proyectos
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "members", ignore = true) // Se agregan después
    @Mapping(target = "createdBy", ignore = true) // Se setea en el servicio
    @Mapping(target = "processes", ignore = true)
    @Mapping(target = "archived", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project createRequestToProject(ProjectDTO.CreateRequest request);

    /**
     * Mapea Project a Response (completo)
     * Incluye miembros mapeados a UserDTO.Summary
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "members", source = "members")
    ProjectDTO.Response projectToResponse(Project project);

    /**
     * Mapea Project a Summary (versión resumida)
     */
    @Mapping(target = "memberCount", expression = "java(project.getMembers().size())")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    ProjectDTO.Summary projectToSummary(Project project);

    /**
     * Actualiza un Project existente con datos de UpdateRequest
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "processes", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    void updateProjectFromDto(ProjectDTO.UpdateRequest request, @MappingTarget Project project);

    /**
     * Convierte enum ProjectStatus a String
     */
    @Named("statusToString")
    default String statusToString(Project.ProjectStatus status) {
        return status != null ? status.name() : null;
    }

    /**
     * Convierte String a enum ProjectStatus
     */
    @Named("stringToStatus")
    default Project.ProjectStatus stringToStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return Project.ProjectStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}