package com.taskmanager.controller;

import com.taskmanager.dto.TaskDTO;
import com.taskmanager.model.User;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * ===================================================================
 * Controlador adicional para tareas de un proceso específico
 * ===================================================================
 */
@RestController
@RequestMapping("/api/processes/{processId}/tasks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
class ProcessTaskController {

    private final TaskService taskService;

    /**
     * N°6: Obtiene tareas de un proceso (columna Kanban)
     * GET /api/processes/{processId}/tasks
     *
     * Usa Query Method: findByProcessIdOrderByPositionAsc
     */
    @GetMapping
    public ResponseEntity<List<TaskDTO.Response>> getProcessTasks(
            @PathVariable Long processId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/processes/{}/tasks", processId);

        try {
            List<TaskDTO.Response> tasks = taskService.getProcessTasks(
                    processId,
                    currentUser.getId()
            );

            return ResponseEntity.ok(tasks);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }
}