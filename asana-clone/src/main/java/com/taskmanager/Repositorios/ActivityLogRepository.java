package com.taskmanager.Repositorios;

import com.taskmanager.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
/**
 * ===================================================================
 * ActivityLogRepository - Repositorio de Logs de Actividad (N°7)
 *
 * Query Methods sin SQL para seguimiento de avances
 * ===================================================================
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * N°7: Busca actividades de un proyecto ordenadas por fecha
     * Query Method: SELECT * FROM activity_logs WHERE project_id = ? ORDER BY created_at DESC
     */
    List<ActivityLog> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    /**
     * N°7: Busca actividades de un proyecto (con paginación)
     */
    Page<ActivityLog> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    /**
     * N°7: Busca actividades de un usuario en un proyecto
     */
    List<ActivityLog> findByProjectIdAndUserIdOrderByCreatedAtDesc(
            Long projectId,
            Long userId
    );

    /**
     * N°7: Busca actividades por tipo
     */
    List<ActivityLog> findByProjectIdAndActivityType(
            Long projectId,
            ActivityLog.ActivityType activityType
    );

    /**
     * N°7: Busca actividades relacionadas con una entidad
     */
    List<ActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType,
            Long entityId
    );

    /**
     * N°7: Busca actividades en un rango de fechas
     */
    List<ActivityLog> findByProjectIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
@Modifying
    @Query("DELETE FROM ActivityLog a WHERE a.project.id = :projectId")
    int deleteByProjectId(@Param("projectId") Long projectId);
    /**
     * N°7: Cuenta actividades de un proyecto
     */
    long countByProjectId(Long projectId);

    /**
     * N°7: Cuenta actividades por tipo en un proyecto
     */
    long countByProjectIdAndActivityType(
            Long projectId,
            ActivityLog.ActivityType activityType
    );

    /**
     * N°7: Busca actividades recientes (últimas 24 horas)
     */
    @Query("SELECT a FROM ActivityLog a WHERE a.project.id = :projectId " +
            "AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentActivities(
            @Param("projectId") Long projectId,
            @Param("since") LocalDateTime since
    );

    /**
     * N°7: Estadísticas de actividades por usuario
     */
    @Query("SELECT a.user.id as userId, COUNT(a) as count " +
            "FROM ActivityLog a WHERE a.project.id = :projectId " +
            "GROUP BY a.user.id ORDER BY count DESC")
    List<Object[]> countActivitiesByUser(@Param("projectId") Long projectId);

    /**
     * N°7: Estadísticas de actividades por tipo
     */
    @Query("SELECT a.activityType as type, COUNT(a) as count " +
            "FROM ActivityLog a WHERE a.project.id = :projectId " +
            "GROUP BY a.activityType ORDER BY count DESC")
    List<Object[]> countActivitiesByType(@Param("projectId") Long projectId);

    /**
     * N°7: Elimina logs antiguos (limpieza de datos)
     */
    void deleteByCreatedAtBefore(LocalDateTime date);
}
