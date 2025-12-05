package com.taskmanager.service;

import com.taskmanager.dto.NotificationDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.NotificationMapper;
import com.taskmanager.model.Notification;
import com.taskmanager.model.NotificationSettings;
import com.taskmanager.model.User;
import com.taskmanager.Repositorios.NotificationRepository;
import com.taskmanager.Repositorios.NotificationSettingsRepository;
import com.taskmanager.Repositorios.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.Future; // Se podría usar para retornar, pero usaremos 'void' para simplicidad asíncrona

/**
 * SERVICIO CENTRAL DE NOTIFICACIONES
 *
 * - Incluye lógica de configuración (NotificationSettings).
 * - Métodos base para creación de notificaciones (Async/Sync, por objetos/IDs).
 * - Funcionalidades de listado, paginación, marcado, archivado y eliminación.
 * - Métodos específicos para invitaciones y eventos de suscripción (Sprint 4).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    // ======================================================
    // MÉTODO BASE: Crear notificación (Async - Objeto User) - CORREGIDO
    // ======================================================
    /**
     * Crea una notificación de forma asíncrona. Retorna void para compatibilidad con @Async.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNotification( // <<-- CORREGIDO: Retorna VOID
                                    User user, // Objeto del Destinatario
                                    User actor, // Objeto del Actor que desencadena la acción
                                    Notification.NotificationType type,
                                    String title,
                                    String message,
                                    String entityType,
                                    Long entityId,
                                    String actionUrl, // URL de acción explícita
                                    Notification.NotificationPriority priority // Prioridad explícita
    ) {
        log.debug("Creating notification type {} for user {}", type, user.getId());

        // 1. Control de configuración (NotificationSettings)
        NotificationSettings settings = settingsRepository.findByUserId(user.getId()).orElse(null);

        if (settings != null && !settings.shouldNotify(type)) {
            log.debug("User {} has disabled notifications of type {}", user.getId(), type);
            return; // No retorna Notification, retorna void
        }

        Notification notification = Notification.builder()
                .user(user)
                .actor(actor)
                .type(type)
                .priority(priority)
                .title(title != null ? title : (type != null ? type.getDefaultTitle() : null))
                .message(message)
                .entityType(entityType)
                .entityId(entityId)
                .actionUrl(actionUrl)
                .icon(type != null ? type.getDefaultIcon() : null)
                .read(false)
                .archived(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created with ID {}", saved.getId());

        // Se elimina el 'return saved;'
    }

    // ======================================================
    // MÉTODO BASE: Crear notificación (Async - IDs - Delega) - CORREGIDO
    // ======================================================
    /**
     * Crea una notificación a partir de IDs de forma asíncrona. Delega la lógica de guardado al método que usa objetos User.
     */
    @Async
    public void createNotification( // <<-- CORREGIDO: Retorna VOID
                                    Long userId,
                                    Notification.NotificationType type,
                                    String title,
                                    String message,
                                    String entityType,
                                    Long entityId,
                                    Long actorId // ID del usuario que desencadena la acción
    ) {
        log.debug("Creating notification type {} for user {}", type, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));

        User actor = actorId != null ? userRepository.findById(actorId).orElse(null) : null;

        // Determinación de Helpers
        Notification.NotificationPriority priority = determinePriority(type);
        String actionUrl = buildActionUrl(entityType, entityId);

        // 🔥 DELEGAR AL MÉTODO QUE USA OBJETOS USER (Ahora retorna void)
        createNotification(
                user,
                actor,
                type,
                title,
                message,
                entityType,
                entityId,
                actionUrl,
                priority
        );
        // Se elimina el retorno
    }

    // ======================================================
    // MÉTODO BASE: Crear notificación (Sync - IDs - Agregado del segundo archivo)
    // ======================================================
    /**
     * Crea una notificación de forma síncrona.
     * Útil cuando se necesita el objeto Notification inmediatamente después de su creación.
     */
    public Notification createNotificationSync(
            Long userId,
            Notification.NotificationType type,
            String title,
            String message,
            String entityType,
            Long entityId,
            Long actorId) {

        log.info("Creating notification (sync) for user {} - Type: {}", userId, type);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        User actor = actorId != null ? userRepository.findById(actorId).orElse(null) : null;

        // Determinación de Helpers
        Notification.NotificationPriority priority = determinePriority(type);
        String actionUrl = buildActionUrl(entityType, entityId);

        Notification notification = Notification.builder()
                .user(user)
                .actor(actor)
                .type(type)
                .priority(priority)
                .title(title != null ? title : (type != null ? type.getDefaultTitle() : null))
                .message(message)
                .entityType(entityType)
                .entityId(entityId)
                .actionUrl(actionUrl)
                .icon(type != null ? type.getDefaultIcon() : null)
                .read(false)
                .archived(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created successfully (sync) with ID: {}", saved.getId());

        return saved;
    }


    // ======================================================
    // MÉTODOS DE INVITACIÓN DE MIEMBROS (Sprint 4)
    // ======================================================

    /**
     * Notificación enviada al INVITADO cuando recibe una invitación de proyecto.
     */
    @Async
    @Transactional
    public void sendProjectInvitationNotification(
            User invitedUser,
            String invitedEmail,
            String senderName,
            String projectName,
            Long projectId,
            Long invitationId
    ) {
        if (invitedUser != null) {
            log.info("Creating invitation notification for user: {}", invitedUser.getEmail());

            String title = "Invitación a Proyecto";
            String message = String.format(
                    "%s te ha invitado a unirte al proyecto '%s'. ¡Tu talento es requerido!",
                    senderName, projectName
            );

            String actionUrl = "/invitations/" + invitationId;

            createNotification(
                    invitedUser.getId(),
                    Notification.NotificationType.PROJECT_INVITATION_RECEIVED,
                    title,
                    message,
                    "INVITATION",
                    invitationId,
                    null
            );
        } else {
            log.warn("User {} does not exist in DB. Need to send an external email invitation.", invitedEmail);
            // Aquí se llamaría a un EmailService.sendInvitationEmail(...)
        }
    }

    /**
     * Notificación enviada al CREADOR/REMITENTE cuando el invitado ACEPTA.
     */
    @Async
    @Transactional
    public void sendInvitationAcceptedNotification(
            User projectCreator,
            String invitedEmail,
            String projectName
    ) {
        log.info("Creating invitation accepted notification for creator: {}", projectCreator.getEmail());

        String title = "Invitación Aceptada 🎉";
        String message = String.format(
                "%s ha aceptado la invitación al proyecto '%s'. ¡Ahora forma parte del equipo!",
                invitedEmail, projectName
        );

        createNotification(
                projectCreator.getId(),
                Notification.NotificationType.MEMBER_JOINED,
                title,
                message,
                "PROJECT",
                null,
                null
        );
    }

    /**
     * Notificación enviada al CREADOR/REMITENTE cuando el invitado RECHAZA.
     */
    @Async
    @Transactional
    public void sendInvitationRejectedNotification(
            User projectCreator,
            String invitedEmail,
            String projectName
    ) {
        log.info("Creating invitation rejected notification for creator: {}", projectCreator.getEmail());

        String title = "Invitación Rechazada 😢";
        String message = String.format(
                "%s ha rechazado unirse al proyecto '%s'. Invitación cancelada.",
                invitedEmail, projectName
        );

        createNotification(
                projectCreator.getId(),
                Notification.NotificationType.MEMBER_INVITE_REJECTED,
                title,
                message,
                "PROJECT",
                null,
                null
        );
    }

    // ======================================================
    // MÉTODOS HELPER DEL SPRINT 3 (CREAR NOTIFICACIONES COMUNES)
    // ======================================================

    @Async
    @Transactional
    public void createTaskAssignedNotification(User user, String taskTitle, Long taskId, Long actorId) {
        createNotification(
                user.getId(),
                Notification.NotificationType.TASK_ASSIGNED,
                "New Task Assigned",
                "You have been assigned to task: " + taskTitle,
                "TASK",
                taskId,
                actorId
        );
    }

    @Async
    @Transactional
    public void createCommentNotification(User user, String commenterName, String taskTitle, Long taskId, Long actorId) {
        createNotification(
                user.getId(),
                Notification.NotificationType.TASK_COMMENTED,
                "New Comment",
                commenterName + " commented on task: " + taskTitle,
                "TASK",
                taskId,
                actorId
        );
    }

    @Async
    @Transactional
    public void createMentionNotification(User user, String mentionerName, String taskTitle, Long taskId, Long actorId) {
        createNotification(
                user.getId(),
                Notification.NotificationType.MENTIONED_IN_COMMENT,
                "You were mentioned",
                mentionerName + " mentioned you in task: " + taskTitle,
                "TASK",
                taskId,
                actorId
        );
    }
    // ======================================================
    // MÉTODOS NUEVOS DEL SPRINT 4 (Suscripción/Trial/Exportación)
    // ======================================================

    @Async
    @Transactional
    public void createSubscriptionExpiryNotification(User user, LocalDateTime expiryDate) {
        log.info("Creating subscription expiry notification for user: {}", user.getEmail());

        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDateTime.now(), expiryDate);

        String title = "Subscription Expiring Soon";
        String message = String.format(
                "Your subscription will expire in %d day%s on %s. Please renew to continue using premium features.",
                daysUntilExpiry,
                daysUntilExpiry > 1 ? "s" : "",
                expiryDate.toLocalDate()
        );

        createNotification(
                user.getId(),
                Notification.NotificationType.SUBSCRIPTION_RENEWAL,
                title,
                message,
                null,
                null,
                null
        );

        log.info("Subscription expiry notification created successfully");
    }

    @Async
    @Transactional
    public void createTrialEndingNotification(User user, LocalDateTime trialEndDate) {
        log.info("Creating trial ending notification for user: {}", user.getEmail());

        long daysUntilEnd = ChronoUnit.DAYS.between(LocalDateTime.now(), trialEndDate);

        String title = "Trial Period Ending Soon";
        String message;
        if (daysUntilEnd <= 1) {
            message = "Your trial period ends today! Upgrade to a paid plan to keep all your data and continue using premium features.";
        } else {
            message = String.format("Your trial period ends in %d day%s. Upgrade now to unlock all premium features and continue your work seamlessly.",
                    daysUntilEnd, daysUntilEnd > 1 ? "s" : "");
        }

        createNotification(
                user.getId(),
                Notification.NotificationType.SUBSCRIPTION_TRIAL_ENDING,
                title,
                message,
                null,
                null,
                null
        );

        log.info("Trial ending notification created successfully");
    }

    @Async
    @Transactional
    public void createReminderNotification(User user, String title, String message, Long referenceId) {
        log.info("Creating reminder notification for user: {}", user.getEmail());

        createNotification(
                user.getId(),
                Notification.NotificationType.REMINDER,
                title,
                message,
                null,
                referenceId,
                null
        );

        log.info("Reminder notification created successfully");
    }

    @Async
    @Transactional
    public void createPlanChangedNotification(User user, String oldPlan, String newPlan) {
        log.info("Creating plan changed notification for user: {}", user.getEmail());

        String title = "Subscription Plan Changed";
        String message = String.format("Your subscription has been changed from %s to %s. The changes will take effect immediately.", oldPlan, newPlan);

        createNotification(
                user.getId(),
                Notification.NotificationType.SUBSCRIPTION_CHANGED,
                title,
                message,
                null,
                null,
                null
        );

        log.info("Plan changed notification created successfully");
    }

    @Async
    @Transactional
    public void createExportCompletedNotification(User user, String fileName, Long exportJobId) {
        log.info("Creating export completed notification for user: {}", user.getEmail());

        String title = "Export Completed";
        String message = String.format("Your export '%s' is ready for download. The file will be available for 7 days.", fileName);

        createNotification(
                user.getId(),
                Notification.NotificationType.EXPORT_READY,
                title,
                message,
                "EXPORT",
                exportJobId,
                null
        );

        log.info("Export completed notification created successfully");
    }

    // ======================================================
    // OBTENER / PAGINAR / CONTAR
    // ======================================================

    /**
     * Obtener todas las notificaciones del usuario, opcionalmente incluyendo las leídas.
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO.Response> getUserNotifications(Long userId, Boolean includeRead) {
        log.info("Fetching notifications for user ID: {}, includeRead: {}", userId, includeRead);

        List<Notification> notifications;
        if (Boolean.FALSE.equals(includeRead)) {
            // Buscamos solo las no leídas
            notifications = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        } else {
            // Buscamos todas (leídas y no leídas)
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return notifications.stream()
                .map(notificationMapper::notificationToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener notificaciones paginadas
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO.Response> getUserNotificationsPaginated(
            Long userId,
            int page,
            int size,
            Boolean includeRead
    ) {
        log.info("Fetching paginated notifications for user ID: {}", userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Notification> notifications;
        if (Boolean.FALSE.equals(includeRead)) {
            notifications = notificationRepository.findByUserIdAndReadFalse(userId, pageable);
        } else {
            notifications = notificationRepository.findByUserId(userId, pageable);
        }

        return notifications.map(notificationMapper::notificationToResponse);
    }

    /**
     * Obtiene notificaciones por tipo
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO.Response> getNotificationsByType(
            Long userId,
            Notification.NotificationType type) {

        log.debug("Fetching notifications of type {} for user {}", type, userId);

        List<Notification> notifications = notificationRepository
                .findByUserIdAndType(userId, type);

        return notifications.stream()
                .map(notificationMapper::notificationToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Contar notificaciones no leídas
     */
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    // ======================================================
    // MARCADO / ARCHIVO / BORRADO
    // ======================================================

    /**
     * Marcar notificación como leída (valida pertenencia)
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification {} as read by user {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("La notificación no pertenece al usuario");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /**
     * Marcar múltiples como leídas
     */
    @Transactional
    public void markMultipleAsRead(Set<Long> notificationIds, Long userId) {
        log.info("Marking {} notifications as read for user {}", notificationIds.size(), userId);

        notificationRepository.findAllById(notificationIds).stream()
                .filter(n -> n.getUser().getId().equals(userId))
                .forEach(n -> {
                    n.markAsRead();
                    notificationRepository.save(n);
                });
    }

    /**
     * Marcar todas como leídas
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user ID: {}", userId);

        notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .forEach(n -> {
                    n.markAsRead();
                    notificationRepository.save(n);
                });
    }

    /**
     * Archivar notificación (valida pertenencia)
     */
    @Transactional
    public void archiveNotification(Long notificationId, Long userId) {
        log.info("Archiving notification {} for user {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("La notificación no pertenece al usuario");
        }

        notification.setArchived(true);
        notificationRepository.save(notification);
    }

    /**
     * Eliminar notificación (valida pertenencia)
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        log.info("Deleting notification {} for user {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("La notificación no pertenece al usuario");
        }

        notificationRepository.delete(notification);
    }

    /**
     * Elimina notificaciones antiguas (housekeeping)
     */
    @Async
    @Transactional
    public void deleteOldNotifications(int daysOld) {
        log.info("Deleting notifications older than {} days", daysOld);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);

        // Asumiendo que existe findByCreatedAtBefore en el repositorio
        List<Notification> oldNotifications = notificationRepository
                .findByCreatedAtBefore(cutoffDate);

        notificationRepository.deleteAll(oldNotifications);
        log.info("Deleted {} old notifications", oldNotifications.size());
    }

    // ======================================================
    // UTILITIES
    // ======================================================

    private Notification.NotificationPriority determinePriority(Notification.NotificationType type) {
        if (type == null) return Notification.NotificationPriority.LOW;

        return switch (type) {
            case TASK_OVERDUE, SYSTEM_MAINTENANCE -> Notification.NotificationPriority.URGENT;
            case TASK_DEADLINE_APPROACHING, PROJECT_DEADLINE_APPROACHING, MENTIONED_IN_COMMENT ->
                    Notification.NotificationPriority.HIGH;
            case TASK_ASSIGNED, TASK_STATUS_CHANGED, PROJECT_STATUS_CHANGED ->
                    Notification.NotificationPriority.NORMAL;
            default -> Notification.NotificationPriority.LOW;
        };
    }

    private String buildActionUrl(String entityType, Long entityId) {
        if (entityType == null || entityId == null) return null;

        return switch (entityType) {
            case "TASK" -> "/tasks/" + entityId;
            case "PROJECT" -> "/projects/" + entityId;
            case "COMMENT" -> "/comments/" + entityId;
            case "EXPORT" -> "/exports/" + entityId + "/download";
            case "INVITATION" -> "/invitations/" + entityId;
            default -> null;
        };
    }
}