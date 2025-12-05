package com.taskmanager.security;

// ===================================
// 2. CURRENT USER ANNOTATION
// Ubicación: com.taskmanager.security
// ===================================

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Anotación personalizada para inyectar el usuario actual autenticado
 * Simplifica: @AuthenticationPrincipal UserPrincipal currentUser
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
