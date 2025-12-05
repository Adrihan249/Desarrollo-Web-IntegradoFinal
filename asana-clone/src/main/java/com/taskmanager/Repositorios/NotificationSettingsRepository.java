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
 * NotificationSettingsRepository - Configuración de Notificaciones (N°15)
 * ===================================================================
 */
@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {

    /**
     * N°15: Busca configuración de un usuario
     * Query Method: SELECT * FROM notification_settings WHERE user_id = ?
     */
    Optional<NotificationSettings> findByUserId(Long userId);

    /**
     * N°15: Verifica si existe configuración para un usuario
     */
    boolean existsByUserId(Long userId);

    /**
     * N°15: Busca usuarios con notificaciones habilitadas
     */
    List<NotificationSettings> findByNotificationsEnabledTrue();

    /**
     * N°15: Busca usuarios con resumen diario habilitado
     */
    List<NotificationSettings> findByDailyEmailSummaryTrue();

    /**
     * N°15: Busca usuarios con resumen semanal habilitado
     */
    List<NotificationSettings> findByWeeklyEmailSummaryTrue();
}
