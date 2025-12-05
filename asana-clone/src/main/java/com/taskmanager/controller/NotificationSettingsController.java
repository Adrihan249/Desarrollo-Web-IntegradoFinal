package com.taskmanager.controller;

import com.taskmanager.dto.NotificationDTO;
import com.taskmanager.dto.NotificationSettingsDTO;
import com.taskmanager.model.User;
import com.taskmanager.service.NotificationService;
import com.taskmanager.service.NotificationSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
/**
 * ===================================================================
 * Controlador REST de Configuración de Notificaciones
 *
 * CUMPLE REQUERIMIENTO N°15: Configuración de notificaciones
 * ===================================================================
 */
@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
class NotificationSettingsController {

    private final NotificationSettingsService settingsService;

    /**
     * N°15: Obtiene configuración del usuario
     * GET /api/notifications/settings
     */
    @GetMapping
    public ResponseEntity<NotificationSettingsDTO.Response> getSettings(
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/notifications/settings for user: {}", currentUser.getEmail());

        NotificationSettingsDTO.Response settings = settingsService
                .getUserSettings(currentUser.getId());

        return ResponseEntity.ok(settings);
    }

    /**
     * N°15: Actualiza configuración
     * PUT /api/notifications/settings
     */
    @PutMapping
    public ResponseEntity<NotificationSettingsDTO.Response> updateSettings(
            @Valid @RequestBody NotificationSettingsDTO.UpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/notifications/settings for user: {}", currentUser.getEmail());

        try {
            NotificationSettingsDTO.Response settings = settingsService
                    .updateSettings(currentUser.getId(), request);

            return ResponseEntity.ok(settings);

        } catch (IllegalArgumentException e) {
            log.error("Invalid settings: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    /**
     * N°15: Restablece configuración a valores por defecto
     * POST /api/notifications/settings/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<NotificationSettingsDTO.Response> resetToDefaults(
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/notifications/settings/reset for user: {}", currentUser.getEmail());

        NotificationSettingsDTO.Response settings = settingsService
                .resetToDefaults(currentUser.getId());

        return ResponseEntity.ok(settings);
    }

    /**
     * N°15: Activa/desactiva todas las notificaciones
     * PUT /api/notifications/settings/toggle-all
     */
    @PutMapping("/toggle-all")
    public ResponseEntity<NotificationSettingsDTO.Response> toggleAllNotifications(
            @RequestParam boolean enabled,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/notifications/settings/toggle-all?enabled={}", enabled);

        NotificationSettingsDTO.Response settings = settingsService
                .toggleAllNotifications(currentUser.getId(), enabled);

        return ResponseEntity.ok(settings);
    }

    /**
     * N°15: Activa/desactiva modo "No molestar"
     * PUT /api/notifications/settings/do-not-disturb
     */
    @PutMapping("/do-not-disturb")
    public ResponseEntity<NotificationSettingsDTO.Response> toggleDoNotDisturb(
            @RequestParam boolean enabled,
            @RequestParam(required = false) Integer startHour,
            @RequestParam(required = false) Integer endHour,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/notifications/settings/do-not-disturb?enabled={}", enabled);

        try {
            NotificationSettingsDTO.Response settings = settingsService
                    .toggleDoNotDisturb(currentUser.getId(), enabled, startHour, endHour);

            return ResponseEntity.ok(settings);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }
}