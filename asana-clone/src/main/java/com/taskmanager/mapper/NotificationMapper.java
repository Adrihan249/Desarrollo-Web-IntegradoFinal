package com.taskmanager.mapper;

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NotificationMapper - Mapeo entre Notification entities y DTOs
 *
 * CUMPLE REQUERIMIENTO N°8: Notificaciones internas
 */
@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface NotificationMapper {

    /**
     * N°8: Mapea Notification a Response
     */
    @Mapping(target = "type", source = "type", qualifiedByName = "typeToString")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "priorityToString")
    NotificationDTO.Response notificationToResponse(Notification notification);

    /**
     * N°8: Mapea Notification a Summary
     */
    @Mapping(target = "type", source = "type", qualifiedByName = "typeToString")
    NotificationDTO.Summary notificationToSummary(Notification notification);

    /**
     * Lista de Notifications a Responses
     */
    List<NotificationDTO.Response> notificationsToResponses(List<Notification> notifications);

    @Named("typeToString")
    default String typeToString(Notification.NotificationType type) {
        return type != null ? type.name() : null;
    }

    @Named("priorityToString")
    default String priorityToString(Notification.NotificationPriority priority) {
        return priority != null ? priority.name() : null;
    }
}
