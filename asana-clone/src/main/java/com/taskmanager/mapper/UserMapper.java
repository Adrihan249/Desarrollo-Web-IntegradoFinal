package com.taskmanager.mapper;

import com.taskmanager.dto.UserDTO;
import com.taskmanager.model.Role;
import com.taskmanager.model.User;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * UserMapper - Usa MapStruct para mapeo automático entre entidades y DTOs
 *
 * MapStruct genera automáticamente la implementación de este mapper en tiempo de compilación.
 * Esto elimina código boilerplate y mejora el rendimiento vs reflexión.
 *
 * CUMPLE REQUERIMIENTOS:
 * - N°1: Autenticación (mapeo de registro y login)
 * - N°9: Gestión de usuarios (mapeo de operaciones CRUD)
 * - N°16: Personalización de perfil (mapeo de actualización)
 */
@Mapper(
        componentModel = "spring", // Genera un @Component de Spring
        unmappedTargetPolicy = ReportingPolicy.IGNORE, // Ignora campos no mapeados
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE // No sobrescribe con nulls
)
public interface UserMapper {

    /**
     * Mapea RegisterRequest a User
     * Usado en N°1: Autenticación (registro)
     *
     * @Mapping ignora campos que se setean después (id, roles, etc.)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "createdProjects", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    User registerRequestToUser(UserDTO.RegisterRequest request);

    /**
     * Mapea User a Response
     * Convierte Set<Role> a Set<String> con los nombres de los roles
     */
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    UserDTO.Response userToResponse(User user);
    /**
     * Mapea User a Summary (versión resumida)
     */
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserDTO.Summary userToSummary(User user);


    /**
     * Actualiza un User existente con datos de UpdateRequest
     * Solo actualiza campos no nulos del request
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true) // Email no se puede cambiar
    @Mapping(target = "password", ignore = true) // Password se cambia por otro endpoint
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "createdProjects", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserFromDto(UserDTO.UpdateRequest request, @MappingTarget User user);

    /**
     * Método custom para convertir Set<Role> a Set<String>
     * Extrae solo los nombres de los roles
     */
    @Named("rolesToNames")
    default Set<String> rolesToNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}