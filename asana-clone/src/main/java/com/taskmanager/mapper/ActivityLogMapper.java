package com.taskmanager.mapper;
import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ===================================================================
 * ActivityLogMapper - Mapeo entre ActivityLog entities y DTOs
 *
 * CUMPLE REQUERIMIENTO N°7: Seguimiento de avances
 * ===================================================================
 */
@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ActivityLogMapper {

    /**
     * N°7: Mapea ActivityLog a Response
     */
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "activityType", source = "activityType", qualifiedByName = "typeToString")
    @Mapping(target = "icon", expression = "java(getActivityIcon(activityLog))")
    ActivityLogDTO.Response activityLogToResponse(ActivityLog activityLog);

    /**
     * Lista de ActivityLogs a Responses
     */
    List<ActivityLogDTO.Response> activityLogsToResponses(List<ActivityLog> activityLogs);

    @Named("typeToString")
    default String typeToString(ActivityLog.ActivityType type) {
        return type != null ? type.name() : null;
    }

    /**
     * Obtiene el icono de la actividad
     */
    default String getActivityIcon(ActivityLog activityLog) {
        return activityLog.getActivityType() != null
                ? activityLog.getActivityType().getIcon()
                : "📝";
    }
}