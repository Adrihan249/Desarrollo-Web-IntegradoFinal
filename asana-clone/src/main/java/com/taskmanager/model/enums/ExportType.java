// ===================================
// EXPORT TYPE
// ===================================
package com.taskmanager.model.enums;

public enum ExportType {
    PROJECT_FULL,      // Proyecto completo (tareas, comentarios, archivos)
    TASKS_ONLY,        // Solo tareas
    ACTIVITIES,        // Timeline de actividades
    COMMENTS,          // Comentarios
    SUBSCRIPTION_REPORT, // Reporte de suscripción
    USER_DATA          // Datos del usuario (GDPR compliance)
}