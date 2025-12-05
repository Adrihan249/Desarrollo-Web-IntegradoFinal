// ===================================
// REMINDER TYPE
// ===================================
package com.taskmanager.model.enums;

public enum ReminderType {
    TASK_DEADLINE,        // Recordatorio de tarea próxima a vencer
    MEETING,              // Recordatorio de reunión
    SUBSCRIPTION_RENEWAL, // Renovación de suscripción
    SUBSCRIPTION_EXPIRY,  // Suscripción por vencer
    PROJECT_DEADLINE,     // Deadline de proyecto
    COMMENT_MENTION,      // Alguien te mencionó
    CUSTOM                // Recordatorio personalizado
}
