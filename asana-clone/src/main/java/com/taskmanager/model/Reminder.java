// ===================================
// REMINDER ENTITY
// ===================================
package com.taskmanager.model;

import com.taskmanager.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "reminders", indexes = {
        @Index(name = "idx_reminder_user", columnList = "user_id"),
        @Index(name = "idx_reminder_status", columnList = "status"),
        @Index(name = "idx_reminder_date", columnList = "reminder_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // TIPO Y REFERENCIA
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReminderType type;

    private Long referenceId; // ID de la tarea/proyecto/suscripción

    @Column(length = 50)
    private String referenceType; // "Task", "Project", "Subscription"

    // CONTENIDO
    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String message;

    // PROGRAMACIÓN
    @Column(nullable = false)
    private LocalDateTime reminderDate;

    private LocalDateTime snoozeUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReminderFrequency frequency = ReminderFrequency.ONCE;

    private Integer advanceMinutes; // Cuántos minutos antes recordar

    // ESTADO
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReminderStatus status = ReminderStatus.PENDING;

    private LocalDateTime sentAt;

    private LocalDateTime dismissedAt;

    // CANALES DE NOTIFICACIÓN
    @Column(nullable = false)
    private Boolean emailNotification = false;

    @Column(nullable = false)
    private Boolean inAppNotification = true;

    @Column(nullable = false)
    private Boolean pushNotification = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
