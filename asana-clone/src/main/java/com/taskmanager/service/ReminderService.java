package com.taskmanager.service;

import com.taskmanager.dto.ReminderDTO;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.ReminderMapper;
import com.taskmanager.model.*;
import com.taskmanager.model.enums.*;
import com.taskmanager.Repositorios.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException; // <<-- IMPORTADO
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// ===================================
// REMINDER SERVICE
// ===================================
@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final ReminderMapper reminderMapper;
    private final NotificationService notificationService;

    /**
     * Crear recordatorio
     */
    @Transactional
    public ReminderDTO.Response createReminder(Long userId, ReminderDTO.CreateRequest request) {
        log.info("Creating reminder for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (request.getReminderDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reminder date must be in the future");
        }

        Reminder reminder = reminderMapper.toEntity(request);
        reminder.setUser(user);
        reminder = reminderRepository.save(reminder);

        log.info("Reminder created successfully with ID: {}", reminder.getId());
        return reminderMapper.toResponse(reminder);
    }

    /**
     * Crear recordatorio desde tarea
     */
    @Transactional
    public ReminderDTO.Response createTaskReminder(
            Long userId,
            Long taskId,
            Integer advanceMinutes,
            LocalDateTime dueDate
    ) {
        log.info("Creating task reminder for user ID: {} and task ID: {}", userId, taskId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDateTime reminderDate = dueDate.minusMinutes(advanceMinutes);

        if (reminderDate.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Calculated reminder date is in the past");
        }

        Reminder reminder = Reminder.builder()
                .user(user)
                .type(ReminderType.TASK_DEADLINE)
                .referenceId(taskId)
                .referenceType("Task")
                .title("Task Deadline Reminder")
                .message("Task is due soon")
                .reminderDate(reminderDate)
                .frequency(ReminderFrequency.ONCE)
                .advanceMinutes(advanceMinutes)
                .status(ReminderStatus.PENDING)
                .emailNotification(true)
                .inAppNotification(true)
                .pushNotification(false)
                .build();

        reminder = reminderRepository.save(reminder);

        log.info("Task reminder created successfully");
        return reminderMapper.toResponse(reminder);
    }

    /**
     * Obtener recordatorios del usuario
     */
    @Transactional(readOnly = true)
    public List<ReminderDTO.Response> getUserReminders(Long userId, ReminderStatus status) {
        log.info("Fetching reminders for user ID: {} with status: {}", userId, status);

        List<Reminder> reminders = status != null
                ? reminderRepository.findByUserIdAndStatusOrderByReminderDateAsc(userId, status)
                : reminderRepository.findByUserIdOrderByReminderDateAsc(userId);

        return reminders.stream()
                .map(reminderMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener recordatorio por ID
     *
     * [CORRECCIÓN SEGURIDAD] Cambiado BadRequestException por AccessDeniedException.
     */
    @Transactional(readOnly = true)
    public ReminderDTO.Response getReminderById(Long reminderId, Long userId) {
        log.info("Fetching reminder ID: {} for user ID: {}", reminderId, userId);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with ID: " + reminderId));

        if (!reminder.getUser().getId().equals(userId)) {
            // >> CORRECCIÓN: Excepción de acceso
            throw new AccessDeniedException("Reminder does not belong to user");
        }

        return reminderMapper.toResponse(reminder);
    }

    /**
     * Actualizar recordatorio
     *
     * [CORRECCIÓN SEGURIDAD] Cambiado BadRequestException por AccessDeniedException.
     */
    @Transactional
    public ReminderDTO.Response updateReminder(
            Long reminderId,
            Long userId,
            ReminderDTO.UpdateRequest request
    ) {
        log.info("Updating reminder ID: {} for user ID: {}", reminderId, userId);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));

        if (!reminder.getUser().getId().equals(userId)) {
            // >> CORRECCIÓN: Excepción de acceso
            throw new AccessDeniedException("Reminder does not belong to user");
        }

        if (reminder.getStatus() == ReminderStatus.SENT) {
            throw new BadRequestException("Cannot update already sent reminder");
        }

        reminderMapper.updateEntityFromRequest(request, reminder);
        reminder = reminderRepository.save(reminder);

        log.info("Reminder updated successfully");
        return reminderMapper.toResponse(reminder);
    }

    /**
     * Posponer recordatorio (snooze)
     *
     * [CORRECCIÓN SEGURIDAD] Cambiado BadRequestException por AccessDeniedException.
     */
    @Transactional
    public ReminderDTO.Response snoozeReminder(
            Long reminderId,
            Long userId,
            ReminderDTO.SnoozeRequest request
    ) {
        log.info("Snoozing reminder ID: {} for {} minutes", reminderId, request.getMinutes());

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));

        if (!reminder.getUser().getId().equals(userId)) {
            // >> CORRECCIÓN: Excepción de acceso
            throw new AccessDeniedException("Reminder does not belong to user");
        }

        LocalDateTime snoozeUntil = LocalDateTime.now().plusMinutes(request.getMinutes());
        reminder.setSnoozeUntil(snoozeUntil);
        reminder.setStatus(ReminderStatus.SNOOZED);

        reminder = reminderRepository.save(reminder);

        log.info("Reminder snoozed until: {}", snoozeUntil);
        return reminderMapper.toResponse(reminder);
    }

    /**
     * Descartar recordatorio
     *
     * [CORRECCIÓN SEGURIDAD] Cambiado BadRequestException por AccessDeniedException.
     */
    @Transactional
    public void dismissReminder(Long reminderId, Long userId) {
        log.info("Dismissing reminder ID: {}", reminderId);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));

        if (!reminder.getUser().getId().equals(userId)) {
            // >> CORRECCIÓN: Excepción de acceso
            throw new AccessDeniedException("Reminder does not belong to user");
        }

        reminder.setStatus(ReminderStatus.DISMISSED);
        reminder.setDismissedAt(LocalDateTime.now());
        reminderRepository.save(reminder);

        log.info("Reminder dismissed successfully");
    }

    /**
     * Eliminar recordatorio
     *
     * [CORRECCIÓN SEGURIDAD] Cambiado BadRequestException por AccessDeniedException.
     */
    @Transactional
    public void deleteReminder(Long reminderId, Long userId) {
        log.info("Deleting reminder ID: {}", reminderId);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));

        if (!reminder.getUser().getId().equals(userId)) {
            // >> CORRECCIÓN: Excepción de acceso
            throw new AccessDeniedException("Reminder does not belong to user");
        }

        reminderRepository.delete(reminder);
        log.info("Reminder deleted successfully");
    }

    /**
     * Obtener recordatorios de hoy
     */
    @Transactional(readOnly = true)
    public List<ReminderDTO.Response> getTodayReminders(Long userId) {
        log.info("Fetching today's reminders for user ID: {}", userId);

        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        List<Reminder> reminders =
                reminderRepository.findTodayRemindersByUser(userId, start, end);

        return reminders.stream()
                .map(reminderMapper::toResponse)
                .collect(Collectors.toList());
    }


    /**
     * Contar recordatorios pendientes
     */
    @Transactional(readOnly = true)
    public long countPendingReminders(Long userId) {
        return reminderRepository.countByUserIdAndStatus(userId, ReminderStatus.PENDING);
    }

    /**
     * Enviar recordatorio (usado por SchedulerService)
     */
    @Async
    @Transactional
    public void sendReminder(Reminder reminder) {
        log.info("Sending reminder ID: {}", reminder.getId());

        try {
            if (Boolean.TRUE.equals(reminder.getInAppNotification())) {
                notificationService.createReminderNotification(
                        reminder.getUser(),
                        reminder.getTitle(),
                        reminder.getMessage(),
                        reminder.getReferenceId()
                );
            }

            if (Boolean.TRUE.equals(reminder.getEmailNotification())) {
                log.info("Email notification would be sent to: {}", reminder.getUser().getEmail());
            }

            if (Boolean.TRUE.equals(reminder.getPushNotification())) {
                log.info("Push notification would be sent");
            }

            reminder.setStatus(ReminderStatus.SENT);
            reminder.setSentAt(LocalDateTime.now());
            reminderRepository.save(reminder);

            if (reminder.getFrequency() != ReminderFrequency.ONCE) {
                createNextRecurrence(reminder);
            }

            log.info("Reminder sent successfully");

        } catch (Exception e) {
            log.error("Failed to send reminder ID: {}", reminder.getId(), e);
            reminder.setStatus(ReminderStatus.FAILED);
            reminderRepository.save(reminder);
        }
    }

    /**
     * Crear siguiente ocurrencia para recordatorios recurrentes
     */
    private void createNextRecurrence(Reminder originalReminder) {
        LocalDateTime nextDate = calculateNextOccurrence(
                originalReminder.getReminderDate(),
                originalReminder.getFrequency()
        );

        Reminder nextReminder = Reminder.builder()
                .user(originalReminder.getUser())
                .type(originalReminder.getType())
                .referenceId(originalReminder.getReferenceId())
                .referenceType(originalReminder.getReferenceType())
                .title(originalReminder.getTitle())
                .message(originalReminder.getMessage())
                .reminderDate(nextDate)
                .frequency(originalReminder.getFrequency())
                .advanceMinutes(originalReminder.getAdvanceMinutes())
                .status(ReminderStatus.PENDING)
                .emailNotification(originalReminder.getEmailNotification())
                .inAppNotification(originalReminder.getInAppNotification())
                .pushNotification(originalReminder.getPushNotification())
                .build();

        reminderRepository.save(nextReminder);
        log.info("Created next recurrence for reminder, next date: {}", nextDate);
    }

    private LocalDateTime calculateNextOccurrence(LocalDateTime current, ReminderFrequency frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            default -> current;
        };
    }
}