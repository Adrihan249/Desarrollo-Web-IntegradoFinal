package com.taskmanager.model.enums;

public enum NotificationType {
    // ... tipos existentes del Sprint 3 ...
    TASK_ASSIGNED,
    TASK_UPDATED,
    TASK_COMPLETED,
    TASK_COMMENTED,
    TASK_MENTIONED,
    TASK_DUE_SOON,
    TASK_OVERDUE,
    PROJECT_INVITATION,
    PROJECT_REMOVED,
    PROJECT_ROLE_CHANGED,
    MEMBER_JOINED,
    MEMBER_LEFT,
    FILE_UPLOADED,
    FILE_UPDATED,
    DEADLINE_APPROACHING,
    DEADLINE_PASSED,
    SYSTEM_ANNOUNCEMENT,
    SYSTEM_MAINTENANCE,

    // NUEVOS TIPOS DEL SPRINT 4
    SUBSCRIPTION_RENEWAL,        // Renovación de suscripción
    SUBSCRIPTION_TRIAL_ENDING,   // Trial terminando
    SUBSCRIPTION_EXPIRED,         // Suscripción expirada
    SUBSCRIPTION_CHANGED,         // Plan cambiado
    SUBSCRIPTION_CANCELLED,       // Suscripción cancelada
    PAYMENT_SUCCESSFUL,           // Pago exitoso
    PAYMENT_FAILED,               // Pago fallido
    REMINDER,                     // Recordatorio genérico
    EXPORT_READY,                 // Exportación lista
    EXPORT_FAILED                 // Exportación fallida
}