// ===================================
// REMINDER STATUS
// ===================================
package com.taskmanager.model.enums;

public enum ReminderStatus {
    PENDING,   // Pendiente de envío
    SENT,      // Enviado exitosamente
    SNOOZED,   // Pospuesto por el usuario
    DISMISSED, // Descartado por el usuario
    FAILED     // Falló el envío
}