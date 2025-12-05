package com.taskmanager.mapper;

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * ===================================================================
 * NotificationSettingsMapper - Mapeo de Configuración de Notificaciones
 *
 * CUMPLE REQUERIMIENTO N°15: Configuración de notificaciones
 * ===================================================================
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface NotificationSettingsMapper {

    /**
     * N°15: Mapea NotificationSettings a Response
     */
    NotificationSettingsDTO.Response settingsToResponse(NotificationSettings settings);

    /**
     * N°15: Actualiza NotificationSettings desde UpdateRequest
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateSettingsFromDto(
            NotificationSettingsDTO.UpdateRequest request,
            @MappingTarget NotificationSettings settings
    );
}