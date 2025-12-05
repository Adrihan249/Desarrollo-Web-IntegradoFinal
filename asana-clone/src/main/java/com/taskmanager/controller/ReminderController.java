package com.taskmanager.controller;

import com.taskmanager.dto.*;
import com.taskmanager.model.enums.*;
import com.taskmanager.security.CurrentUser;
import com.taskmanager.security.UserPrincipal;
import com.taskmanager.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
// ===================================
// REMINDER CONTROLLER
// ===================================
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
@Slf4j
public class ReminderController {

    private final ReminderService reminderService;

    /**
     * Crear recordatorio
     */
    @PostMapping
    public ResponseEntity<ReminderDTO.Response> createReminder(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ReminderDTO.CreateRequest request
    ) {
        log.info("POST /api/reminders - User: {}", currentUser.getEmail());
        ReminderDTO.Response reminder = reminderService
                .createReminder(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reminder);
    }

    /**
     * Listar recordatorios del usuario
     */
    @GetMapping
    public ResponseEntity<List<ReminderDTO.Response>> getReminders(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) ReminderStatus status
    ) {
        log.info("GET /api/reminders - User: {}, Status: {}",
                currentUser.getEmail(), status);

        List<ReminderDTO.Response> reminders = reminderService
                .getUserReminders(currentUser.getId(), status);

        return ResponseEntity.ok(reminders);
    }

    /**
     * Obtener recordatorio por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReminderDTO.Response> getReminderById(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        log.info("GET /api/reminders/{} - User: {}", id, currentUser.getEmail());
        ReminderDTO.Response reminder = reminderService
                .getReminderById(id, currentUser.getId());
        return ResponseEntity.ok(reminder);
    }

    /**
     * Actualizar recordatorio
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReminderDTO.Response> updateReminder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody ReminderDTO.UpdateRequest request
    ) {
        log.info("PUT /api/reminders/{} - User: {}", id, currentUser.getEmail());
        ReminderDTO.Response reminder = reminderService
                .updateReminder(id, currentUser.getId(), request);
        return ResponseEntity.ok(reminder);
    }

    /**
     * Posponer recordatorio (snooze)
     */
    @PostMapping("/{id}/snooze")
    public ResponseEntity<ReminderDTO.Response> snoozeReminder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody ReminderDTO.SnoozeRequest request
    ) {
        log.info("POST /api/reminders/{}/snooze - User: {}, Minutes: {}",
                id, currentUser.getEmail(), request.getMinutes());

        ReminderDTO.Response reminder = reminderService
                .snoozeReminder(id, currentUser.getId(), request);

        return ResponseEntity.ok(reminder);
    }

    /**
     * Descartar recordatorio
     */
    @PostMapping("/{id}/dismiss")
    public ResponseEntity<Void> dismissReminder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        log.info("POST /api/reminders/{}/dismiss - User: {}", id, currentUser.getEmail());
        reminderService.dismissReminder(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Eliminar recordatorio
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        log.info("DELETE /api/reminders/{} - User: {}", id, currentUser.getEmail());
        reminderService.deleteReminder(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener recordatorios de hoy
     */
    @GetMapping("/today")
    public ResponseEntity<List<ReminderDTO.Response>> getTodayReminders(
            @CurrentUser UserPrincipal currentUser
    ) {
        log.info("GET /api/reminders/today - User: {}", currentUser.getEmail());
        List<ReminderDTO.Response> reminders = reminderService
                .getTodayReminders(currentUser.getId());
        return ResponseEntity.ok(reminders);
    }
}