package com.taskmanager.util;

import com.taskmanager.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

/**
 * Utilidad para acceder al contexto de seguridad de Spring y obtener detalles
 * del usuario autenticado (UserPrincipal).
 */
public final class SecurityContextUtil {

    private SecurityContextUtil() {
        // Clase de utilidad
    }

    /**
     * Obtiene el ID del usuario autenticado actualmente.
     * @return Long ID del usuario.
     * @throws AccessDeniedException si no hay usuario autenticado.
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No hay usuario autenticado.");
        }

        // El objeto 'principal' es donde se guarda nuestra instancia de UserPrincipal
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }

        // Manejar el caso de AnonymousUser o String, aunque no debería ocurrir si está configurado
        throw new AccessDeniedException("Principal desconocido o no es UserPrincipal.");
    }

    /**
     * Obtiene el email del usuario autenticado actualmente.
     * @return String email del usuario.
     * @throws AccessDeniedException si no hay usuario autenticado.
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No hay usuario autenticado.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getEmail();
        }

        throw new AccessDeniedException("Principal desconocido o no es UserPrincipal.");
    }
}