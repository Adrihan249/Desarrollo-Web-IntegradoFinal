package com.taskmanager.controller;

// Archivo: com.taskmanager.controller.UserTaskController.java


import com.taskmanager.dto.TaskDTO;
import com.taskmanager.security.CurrentUser;
import com.taskmanager.security.UserPrincipal;
import com.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks") // Mapeo de nivel superior
@RequiredArgsConstructor
@Slf4j
public class UserTaskController {

    private final TaskService taskService;

    /**
     * Obtiene todas las tareas asignadas al usuario actual.
     * GET /api/tasks/my-tasks
     */
    // GET /api/tasks/my-tasks
    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskDTO.Response>> getMyAssignedTasks(
            @AuthenticationPrincipal(expression = "id") Long currentUserId
    ) {
        log.info("GET /api/tasks/my-tasks - Resolved userId: {}", currentUserId);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<TaskDTO.Response> tasks = taskService.getMyAssignedTasks(currentUserId);
        return ResponseEntity.ok(tasks);
    }


}