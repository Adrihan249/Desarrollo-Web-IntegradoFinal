package com.taskmanager.service;

import com.taskmanager.dto.NotificationSettingsDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.NotificationSettingsMapper;
import com.taskmanager.model.NotificationSettings;
import com.taskmanager.model.User;
import com.taskmanager.Repositorios.NotificationSettingsRepository;
import com.taskmanager.Repositorios.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Gestión de Configuración de Notificaciones
 *
 * CUMPLE REQUERIMIENTO N°15: Configuración de notificaciones
 *
 * Permite a cada usuario personalizar:
 * - Qué tipos de notificaciones recibir
 * - Modo "No molestar"
 * - Resúmenes por email
 * - Alertas de deadline
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationSettingsService {

    private final NotificationSettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final NotificationSettingsMapper settingsMapper;

    /**
     * N°15: Obtiene la configuración de un usuario
     * Si no existe, crea una con valores por defecto
     */
    @Transactional(readOnly = true)
    public NotificationSettingsDTO.Response getUserSettings(Long userId) {
        log.debug("Fetching notification settings for user ID: {}", userId);

        NotificationSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        return settingsMapper.settingsToResponse(settings);
    }

    /**
     * N°15: Actualiza la configuración de un usuario
     */
    public NotificationSettingsDTO.Response updateSettings(
            Long userId,
            NotificationSettingsDTO.UpdateRequest request) {
        log.info("Updating notification settings for user ID: {}", userId);

        // Busca la configuración existente o crea una nueva
        NotificationSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        // Actualiza solo los campos no nulos del request
        settingsMapper.updateSettingsFromDto(request, settings);

        // Valida el horario "No molestar" si está configurado
        if (settings.getDoNotDisturb() &&
                (settings.getDoNotDisturbStartHour() != null ||
                        settings.getDoNotDisturbEndHour() != null)) {

            if (settings.getDoNotDisturbStartHour() == null ||
                    settings.getDoNotDisturbEndHour() == null) {
                throw new IllegalArgumentException(
                        "Debe especificar tanto la hora de inicio como la hora de fin " +
                                "para el modo 'No molestar'"
                );
            }
        }

        NotificationSettings updated = settingsRepository.save(settings);
        log.info("Settings updated successfully for user ID: {}", userId);

        return settingsMapper.settingsToResponse(updated);
    }

    /**
     * N°15: Restablece la configuración a valores por defecto
     */
    public NotificationSettingsDTO.Response resetToDefaults(Long userId) {
        log.info("Resetting notification settings to defaults for user ID: {}", userId);

        NotificationSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        // Restablece todos los valores
        settings.setNotifyTaskAssigned(true);
        settings.setNotifyTaskStatusChanged(true);
        settings.setNotifyTaskDeadlineApproaching(true);
        settings.setNotifyTaskOverdue(true);
        settings.setNotifyTaskCompleted(true);
        settings.setNotifyTaskCommented(true);
        settings.setNotifyTaskAttachmentAdded(false);
        settings.setNotifyMentioned(true);
        settings.setNotifyCommentReplies(true);
        settings.setNotifyProjectAdded(true);
        settings.setNotifyProjectStatusChanged(true);
        settings.setNotifyProjectDeadlineApproaching(true);
        settings.setNotifySubtaskCompleted(false);
        settings.setNotifyAllSubtasksCompleted(true);
        settings.setNotificationsEnabled(true);
        settings.setDailyEmailSummary(false);
        settings.setWeeklyEmailSummary(false);
        settings.setHoursBeforeDeadline(24);
        settings.setDoNotDisturb(false);
        settings.setDoNotDisturbStartHour(null);
        settings.setDoNotDisturbEndHour(null);

        NotificationSettings reset = settingsRepository.save(settings);
        log.info("Settings reset successfully for user ID: {}", userId);

        return settingsMapper.settingsToResponse(reset);
    }

    /**
     * N°15: Activa/desactiva todas las notificaciones
     */
    public NotificationSettingsDTO.Response toggleAllNotifications(
            Long userId,
            boolean enabled) {
        log.info("Toggling all notifications to {} for user ID: {}", enabled, userId);

        NotificationSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setNotificationsEnabled(enabled);

        NotificationSettings updated = settingsRepository.save(settings);

        return settingsMapper.settingsToResponse(updated);
    }

    /**
     * N°15: Activa/desactiva modo "No molestar"
     */
    public NotificationSettingsDTO.Response toggleDoNotDisturb(
            Long userId,
            boolean enabled,
            Integer startHour,
            Integer endHour) {
        log.info("Toggling Do Not Disturb to {} for user ID: {}", enabled, userId);

        NotificationSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setDoNotDisturb(enabled);

        if (enabled) {
            if (startHour == null || endHour == null) {
                throw new IllegalArgumentException(
                        "Debe especificar horas de inicio y fin para 'No molestar'"
                );
            }
            settings.setDoNotDisturbStartHour(startHour);
            settings.setDoNotDisturbEndHour(endHour);
        } else {
            settings.setDoNotDisturbStartHour(null);
            settings.setDoNotDisturbEndHour(null);
        }

        NotificationSettings updated = settingsRepository.save(settings);

        return settingsMapper.settingsToResponse(updated);
    }

    /**
     * N°15: Obtiene usuarios con resúmenes diarios habilitados
     * Para envío programado de emails
     */
    @Transactional(readOnly = true)
    public List<User> getUsersWithDailySummary() {
        log.debug("Fetching users with daily email summary enabled");

        return settingsRepository.findByDailyEmailSummaryTrue().stream()
                .map(NotificationSettings::getUser)
                .collect(Collectors.toList());
    }

    /**
     * N°15: Obtiene usuarios con resúmenes semanales habilitados
     */
    @Transactional(readOnly = true)
    public List<User> getUsersWithWeeklySummary() {
        log.debug("Fetching users with weekly email summary enabled");

        return settingsRepository.findByWeeklyEmailSummaryTrue().stream()
                .map(NotificationSettings::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Crea configuración por defecto para un usuario nuevo
     */
    private NotificationSettings createDefaultSettings(Long userId) {
        log.info("Creating default notification settings for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + userId
                ));

        NotificationSettings settings = NotificationSettings.builder()
                .user(user)
                .build();

        return settingsRepository.save(settings);
    }
}
