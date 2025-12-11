// ===================================
// ACTIVITY LOG SERVICE - CORREGIDO
// ===================================
package com.taskmanager.service;

import com.taskmanager.dto.ActivityLogDTO;
import com.taskmanager.mapper.ActivityLogMapper;
import com.taskmanager.model.*;
import com.taskmanager.Repositorios.ActivityLogRepository;
import com.taskmanager.Repositorios.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de Registro de Actividades
 *
 * CUMPLE REQUERIMIENTO N°7: Seguimiento de avances
 *
 * Registra todas las acciones realizadas en el proyecto
 * para generar timeline y reportes de actividad
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ProjectRepository projectRepository;
    private final ActivityLogMapper activityLogMapper;

    /**
     * N°7: Registra una actividad en el proyecto
     *
     * @param project Proyecto donde ocurrió
     * @param user Usuario que realizó la acción
     * @param type Tipo de actividad
     * @param entityType Tipo de entidad ("TASK", "PROJECT", etc.)
     * @param entityId ID de la entidad
     * @param entityName Nombre/título de la entidad
     */
    @Async // Ejecuta en hilo separado para no bloquear
    public void logActivity(
            Project project,
            User user,
            ActivityLog.ActivityType type,
            String entityType,
            Long entityId,
            String entityName) {

        log.debug("Logging activity: {} by user {} on project {}", type, user.getId(), project.getId());

        // Construye texto legible: "[usuario] creó tarea: 'Título'"
        String description = buildDescription(user, type, entityType, entityName);

        // Construye entidad ActivityLog
        ActivityLog activity = ActivityLog.builder()
                .project(project)
                .user(user)
                .activityType(type)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .description(description)
                .build();

        // Guarda registro de actividad
        activityLogRepository.save(activity);
    }

    /**
     * N°7: Registra actividad con valores anteriores y nuevos
     * Usado para cambios como editar título, fecha, estado, etc.
     */
    @Async
    public void logActivityWithChanges(
            Project project,
            User user,
            ActivityLog.ActivityType type,
            String entityType,
            Long entityId,
            String entityName,
            String previousValue,
            String newValue) {

        log.debug("Logging activity with changes: {} by user {}", type, user.getId());

        // Descripción genérica
        String description = buildDescription(user, type, entityType, entityName);

        // Construye entidad con cambios
        ActivityLog activity = ActivityLog.builder()
                .project(project)
                .user(user)
                .activityType(type)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .description(description)
                .previousValue(previousValue)
                .newValue(newValue)
                .build();

        activityLogRepository.save(activity);
    }

    /**
     * N°7: Obtiene timeline del proyecto
     * Usa Query Method sin SQL:
     * findByProjectIdOrderByCreatedAtDesc
     */
    @Transactional(readOnly = true)
    public List<ActivityLogDTO.Response> getProjectTimeline(
            Long projectId,
            Long userId,
            int page,
            int size) {
        log.debug("Fetching timeline for project ID: {}", projectId);

        Pageable pageable = PageRequest.of(page, size);

        // Timeline paginado
        Page<ActivityLog> activities = activityLogRepository
                .findByProjectIdOrderByCreatedAtDesc(projectId, pageable);

        return activityLogMapper.activityLogsToResponses(activities.getContent());
    }

    /**
     * N°7: Obtiene timeline agrupado por fecha
     * Para mostrar secciones tipo:
     * "Hoy", "Ayer", "2025-11-20"
     */
    @Transactional(readOnly = true)
    public List<ActivityLogDTO.Timeline> getTimelineGroupedByDate(
            Long projectId,
            Long userId) {

        log.debug("Fetching grouped timeline for project ID: {}", projectId);

        // Obtiene actividades sin paginar
        List<ActivityLog> activities = activityLogRepository
                .findByProjectIdOrderByCreatedAtDesc(projectId);

        // Agrupa por fecha (LocalDate)
        Map<LocalDate, List<ActivityLog>> grouped = activities.stream()
                .collect(Collectors.groupingBy(
                        activity -> activity.getCreatedAt().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Convierte a DTO agrupado
        return grouped.entrySet().stream()
                .map(entry -> ActivityLogDTO.Timeline.builder()
                        .date(formatDate(entry.getKey()))
                        .activities(activityLogMapper.activityLogsToResponses(entry.getValue()))
                        .build())
                .collect(Collectors.toList());
    }
@Transactional
public void deleteAllByProjectId(Long projectId) {
    log.info("Deleting all activity logs for project ID: {}", projectId);
    
    int deletedCount = activityLogRepository.deleteByProjectId(projectId);
    
    log.info("Deleted {} activity logs for project ID: {}", deletedCount, projectId);
}
    /**
     * N°7: Obtiene actividades de un usuario en un proyecto
     * Query Method sin SQL:
     * findByProjectIdAndUserIdOrderByCreatedAtDesc
     */
    @Transactional(readOnly = true)
    public List<ActivityLogDTO.Response> getUserActivities(
            Long projectId,
            Long userId) {

        log.debug("Fetching activities for user {} in project {}", userId, projectId);

        List<ActivityLog> activities = activityLogRepository
                .findByProjectIdAndUserIdOrderByCreatedAtDesc(projectId, userId);

        return activityLogMapper.activityLogsToResponses(activities);
    }

    /**
     * N°7: Obtiene actividades dentro de un rango de fechas
     * Query Method sin SQL:
     * findByProjectIdAndCreatedAtBetweenOrderByCreatedAtDesc
     */
    @Transactional(readOnly = true)
    public List<ActivityLogDTO.Response> getActivitiesByDateRange(
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        log.debug("Fetching activities from {} to {}", startDate, endDate);

        List<ActivityLog> activities = activityLogRepository
                .findByProjectIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        projectId, startDate, endDate
                );

        return activityLogMapper.activityLogsToResponses(activities);
    }

    /**
     * N°7: Genera resumen estadístico de actividad
     * Incluye:
     * - total actividades
     * - tareas creadas
     * - tareas completadas
     * - comentarios agregados
     * - archivos subidos
     * - actividades agrupadas por tipo
     * - actividades agrupadas por usuario
     */
    @Transactional(readOnly = true)
    public ActivityLogDTO.Summary getActivitySummary(Long projectId) {

        log.debug("Generating activity summary for project ID: {}", projectId);

        // Conteos individuales
        long totalActivities = activityLogRepository.countByProjectId(projectId);
        long tasksCreated = activityLogRepository.countByProjectIdAndActivityType(
                projectId, ActivityLog.ActivityType.TASK_CREATED
        );
        long tasksCompleted = activityLogRepository.countByProjectIdAndActivityType(
                projectId, ActivityLog.ActivityType.TASK_COMPLETED
        );
        long commentsAdded = activityLogRepository.countByProjectIdAndActivityType(
                projectId, ActivityLog.ActivityType.COMMENT_ADDED
        );
        long filesUploaded = activityLogRepository.countByProjectIdAndActivityType(
                projectId, ActivityLog.ActivityType.ATTACHMENT_UPLOADED
        );

        // Conteo agrupado por tipo
        List<Object[]> byType = activityLogRepository.countActivitiesByType(projectId);
        Map<String, Long> activitiesByType = byType.stream()
                .collect(Collectors.toMap(
                        obj -> ((ActivityLog.ActivityType) obj[0]).name(),
                        obj -> (Long) obj[1]
                ));

        // Conteo agrupado por usuario
        List<Object[]> byUser = activityLogRepository.countActivitiesByUser(projectId);
        Map<String, Long> activitiesByUser = byUser.stream()
                .collect(Collectors.toMap(
                        obj -> obj[0].toString(),
                        obj -> (Long) obj[1]
                ));

        return ActivityLogDTO.Summary.builder()
                .totalActivities(totalActivities)
                .tasksCreated(tasksCreated)
                .tasksCompleted(tasksCompleted)
                .commentsAdded(commentsAdded)
                .filesUploaded(filesUploaded)
                .activitiesByType(activitiesByType)
                .activitiesByUser(activitiesByUser)
                .build();
    }

    /**
     * Construye descripción legible de la actividad
     * Ejemplo:
     * "Juan Pérez creó tarea: Implementar login"
     */
    private String buildDescription(
            User user,
            ActivityLog.ActivityType type,
            String entityType,
            String entityName) {

        return String.format("%s %s %s: %s",
                user.getFullName(),
                type.getActionText(),
                entityType.toLowerCase(),
                entityName);
    }

    /**
     * Formatea fecha para el timeline
     * Convierte:
     * - hoy → "Hoy"
     * - ayer → "Ayer"
     * - otra fecha → YYYY-MM-DD
     */
    private String formatDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        if (date.equals(today)) {
            return "Hoy";
        } else if (date.equals(yesterday)) {
            return "Ayer";
        } else {
            return date.toString();
        }
    }
}
