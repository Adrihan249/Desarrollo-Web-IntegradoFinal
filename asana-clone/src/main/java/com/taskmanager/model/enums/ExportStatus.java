// ===================================
// EXPORT STATUS
// ===================================
package com.taskmanager.model.enums;

public enum ExportStatus {
    PENDING,     // En cola
    PROCESSING,  // Procesando
    COMPLETED,   // Completado
    FAILED,      // Falló
    EXPIRED      // Expirado (archivo eliminado)
}