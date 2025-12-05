package com.taskmanager.model.enums;

// ===================================
// SUBSCRIPTION STATUS
// ===================================
public enum SubscriptionStatus {
    TRIAL,        // En período de prueba
    ACTIVE,       // Activa y pagada
    CANCELLED,    // Cancelada por el usuario
    EXPIRED,      // Expirada (no renovada)
    SUSPENDED,    // Suspendida por falta de pago
    PENDING       // Pendiente de activación
}
