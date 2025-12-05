package com.taskmanager.Repositorios;

import com.taskmanager.model.*;
import com.taskmanager.model.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    // Recordatorios pendientes para enviar
    @Query("SELECT r FROM Reminder r WHERE " +
            "r.status = 'PENDING' AND " +
            "r.reminderDate <= :now AND " +
            "(r.snoozeUntil IS NULL OR r.snoozeUntil <= :now) " +
            "ORDER BY r.reminderDate ASC")
    List<Reminder> findPendingReminders(@Param("now") LocalDateTime now);

    // Recordatorios de un usuario
    List<Reminder> findByUserIdOrderByReminderDateAsc(Long userId);

    // Estado + usuario
    List<Reminder> findByUserIdAndStatusOrderByReminderDateAsc(Long userId, ReminderStatus status);

    // Tipo + Estado
    List<Reminder> findByTypeAndStatus(ReminderType type, ReminderStatus status);

    // Recurrentes
    List<Reminder> findByFrequencyNot(ReminderFrequency frequency);

    // ===============================
    // 🔥 RECORDATORIOS DE HOY (CORRECTO)
    // ===============================
    @Query("""
        SELECT r FROM Reminder r
        WHERE r.reminderDate BETWEEN :start AND :end
          AND r.status = 'PENDING'
        ORDER BY r.reminderDate ASC
    """)
    List<Reminder> findTodayReminders(
            @Param("start") LocalDateTime startOfDay,
            @Param("end") LocalDateTime endOfDay
    );

    @Query("""
        SELECT r FROM Reminder r
        WHERE r.user.id = :userId
          AND r.reminderDate BETWEEN :start AND :end
          AND r.status = 'PENDING'
        ORDER BY r.reminderDate ASC
    """)
    List<Reminder> findTodayRemindersByUser(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime startOfDay,
            @Param("end") LocalDateTime endOfDay
    );

    // Contar pendientes por usuario
    long countByUserIdAndStatus(Long userId, ReminderStatus status);

    // Por referencia (tarea/proyecto)
    List<Reminder> findByReferenceTypeAndReferenceIdOrderByReminderDateAsc(
            String referenceType,
            Long referenceId
    );

    // Vencidos
    @Query("SELECT r FROM Reminder r WHERE " +
            "r.status = 'PENDING' AND " +
            "r.reminderDate < :threshold")
    List<Reminder> findOverdueReminders(@Param("threshold") LocalDateTime threshold);

    // Próximas X horas
    @Query("""
        SELECT r FROM Reminder r
        WHERE r.status = 'PENDING'
          AND r.reminderDate BETWEEN :now AND :endTime
        ORDER BY r.reminderDate ASC
    """)
    List<Reminder> findUpcomingReminders(
            @Param("now") LocalDateTime now,
            @Param("endTime") LocalDateTime endTime
    );
}
