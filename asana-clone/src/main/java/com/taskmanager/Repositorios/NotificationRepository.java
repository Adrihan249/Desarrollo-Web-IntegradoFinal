package com.taskmanager.Repositorios;

import com.taskmanager.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ===================================================================
 * NotificationRepository - Repositorio de Notificaciones (N°8)
 *
 * Query Methods sin SQL para gestión de notificaciones
 * ===================================================================
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * N°8: Busca notificaciones de un usuario ordenadas por fecha
     * Query Method: SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * N°8: Busca notificaciones de un usuario (con paginación)
     */
    Page<Notification> findByUserId(Long userId, Pageable pageable);

    /**
     * N°8: Busca notificaciones no leídas de un usuario
     */
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * N°8: Cuenta notificaciones no leídas
     */
    long countByUserIdAndReadFalse(Long userId);

    /**
     * N°8: Busca notificaciones por tipo
     */
    List<Notification> findByUserIdAndType(Long userId, Notification.NotificationType type);

    /**
     * N°8: Busca notificaciones no archivadas
     */
    List<Notification> findByUserIdAndArchivedFalseOrderByCreatedAtDesc(Long userId);

    /**
     * N°8: Busca notificaciones por prioridad
     */
    List<Notification> findByUserIdAndPriority(
            Long userId,
            Notification.NotificationPriority priority
    );

    /**
     * N°8: Busca notificaciones recientes (últimas 24 horas)
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId " +
            "AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUserId(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );
    /**
     * N°8: Busca notificaciones no leídas de un usuario (paginado)
     */
    Page<Notification> findByUserIdAndReadFalse(Long userId, Pageable pageable);

    /**
     * N°8: Busca notificaciones relacionadas con una entidad
     */
    List<Notification> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * N°8: Elimina notificaciones antiguas
     */
    void deleteByCreatedAtBefore(LocalDateTime date);

    List<Notification> findByCreatedAtBefore(LocalDateTime cutoffDate);
}
