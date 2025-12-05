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
// TASK REMINDER CONTROLLER (Extension)
// ===================================
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskReminderController {

    private final ReminderService reminderService;
    private final TaskService taskService;

    /**
     * Crear recordatorio para tarea
     */
    @PostMapping("/{taskId}/reminder")
    public ResponseEntity<ReminderDTO.Response> createTaskReminder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "60") Integer advanceMinutes
    ) {
        log.info("POST /api/tasks/{}/reminder - User: {}, Advance: {} min",
                taskId, currentUser.getEmail(), advanceMinutes);

        // Obtener tarea para validar y obtener dueDate
        var task = taskService.getTaskById(taskId, currentUser.getId());

        if (task.getDueDate() == null) {
            return ResponseEntity.badRequest().build();
        }

        ReminderDTO.Response reminder = reminderService.createTaskReminder(
                currentUser.getId(),
                taskId,
                advanceMinutes,
                task.getDueDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(reminder);
    }
}
