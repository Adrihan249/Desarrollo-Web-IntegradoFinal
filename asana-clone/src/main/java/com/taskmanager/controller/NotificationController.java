package com.taskmanager.controller;

import com.taskmanager.dto.NotificationDTO;
import com.taskmanager.model.User;
import com.taskmanager.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST de Notificaciones
 *
 * CUMPLE REQUERIMIENTO N°8: Notificaciones internas
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * N°8: Obtiene notificaciones del usuario
     * GET /api/notifications?includeRead=false&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<List<NotificationDTO.Response>> getUserNotifications(
            @RequestParam(defaultValue = "false") boolean includeRead,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("GET /api/notifications for user: {}", currentUser.getEmail());

        List<NotificationDTO.Response> notifications = notificationService
                .getUserNotifications(currentUser.getId(), includeRead);

        return ResponseEntity.ok(notifications);
    }

    /**
     * N°8: Obtiene notificaciones paginadas
     * GET /api/notifications/paginated?page=0&size=20
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<NotificationDTO.Response>> getUserNotificationsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") Boolean includeRead,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page<NotificationDTO.Response> notifications = notificationService
                .getUserNotificationsPaginated(currentUser.getId(), page, size, includeRead);

        return ResponseEntity.ok(notifications);
    }

    /**
     * 🔥 N°8: Cuenta notificaciones no leídas - CORREGIDO
     * GET /api/notifications/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<?> countUnreadNotifications(
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            log.error("Current user is null in countUnreadNotifications");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        try {
            long count = notificationService.countUnreadNotifications(currentUser.getId());
            log.debug("Unread notifications count for user {}: {}", currentUser.getEmail(), count);

            return ResponseEntity.ok(count);

        } catch (Exception e) {
            log.error("Error counting unread notifications for user {}: {}",
                    currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al contar notificaciones"));
        }
    }

    /**
     * N°8: Marca una notificación como leída
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("PUT /api/notifications/{}/read", id);

        try {
            notificationService.markAsRead(id, currentUser.getId());
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Notificación no encontrada"));
        }
    }

    /**
     * N°8: Marca múltiples notificaciones como leídas
     * PUT /api/notifications/mark-read
     */
    @PutMapping("/mark-read")
    public ResponseEntity<?> markMultipleAsRead(
            @RequestBody NotificationDTO.MarkReadRequest request,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("PUT /api/notifications/mark-read - {} notifications",
                request.getNotificationIds().size());

        notificationService.markMultipleAsRead(
                request.getNotificationIds(),
                currentUser.getId()
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * N°8: Marca todas las notificaciones como leídas
     * PUT /api/notifications/mark-all-read
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("PUT /api/notifications/mark-all-read for user: {}", currentUser.getEmail());

        notificationService.markAllAsRead(currentUser.getId());

        return ResponseEntity.noContent().build();
    }

    /**
     * N°8: Archiva una notificación
     * PUT /api/notifications/{id}/archive
     */
    @PutMapping("/{id}/archive")
    public ResponseEntity<?> archiveNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("PUT /api/notifications/{}/archive", id);

        try {
            notificationService.archiveNotification(id, currentUser.getId());
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Notificación no encontrada"));
        }
    }

    /**
     * N°8: Elimina una notificación
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("DELETE /api/notifications/{}", id);

        try {
            notificationService.deleteNotification(id, currentUser.getId());
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Notificación no encontrada"));
        }
    }
}