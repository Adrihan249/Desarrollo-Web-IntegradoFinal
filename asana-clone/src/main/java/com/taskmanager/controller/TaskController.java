package com.taskmanager.controller;

import com.taskmanager.dto.TaskDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.model.User;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException; // ✅ BIEN (Excepción de Seguridad)

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST de Tareas
 *
 * CUMPLE REQUERIMIENTOS:
 * - N°6: Estados de tareas
 * - N°18: Subtareas
 * - N°17: Fecha límite
 * - N°13: Búsqueda y filtros
 */
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class TaskController {

    private final TaskService taskService;

    /**
     * N°6: Crea una nueva tarea
     * POST /api/projects/{projectId}/tasks
     */
    @PostMapping
    public ResponseEntity<TaskDTO.Response> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskDTO.CreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/projects/{}/tasks - Creating task: {}",
                projectId, request.getTitle());

        try {
            TaskDTO.Response task = taskService.createTask(
                    projectId,
                    request,
                    currentUser.getId()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(task);

        } catch (IllegalArgumentException e) {
            log.error("Invalid data: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            log.error("Error creating task: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°6: Obtiene todas las tareas de un proyecto
     * GET /api/projects/{projectId}/tasks
     *
     * Usa Query Method: findByProjectId
     */
    @GetMapping
    public ResponseEntity<List<TaskDTO.Response>> getProjectTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/tasks", projectId);

        try {
            List<TaskDTO.Response> tasks = taskService.getProjectTasks(
                    projectId,
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

    /**
     * N°6: Obtiene una tarea por ID
     * GET /api/projects/{projectId}/tasks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO.Response> getTaskById(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/tasks/{}", projectId, id);

        try {
            TaskDTO.Response task = taskService.getTaskById(
                    id,
                    currentUser.getId()
            );

            return ResponseEntity.ok(task);

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

    /**
     * N°6: Actualiza una tarea
     * PUT /api/projects/{projectId}/tasks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO.Response> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody TaskDTO.UpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/projects/{}/tasks/{}", projectId, id);

        try {
            TaskDTO.Response task = taskService.updateTask(
                    id,
                    request,
                    currentUser.getId()
            );

            return ResponseEntity.ok(task);

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

    /**
     * N°5: Mueve una tarea a otro proceso (columna Kanban)
     * POST /api/projects/{projectId}/tasks/{id}/move
     */

    @PostMapping("/{id}/move")
    public ResponseEntity<?> moveTask(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @RequestBody TaskDTO.MoveRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("POST /api/projects/{}/tasks/{}/move to process {} (Position: {})",
                projectId, id, request.getTargetProcessId(), request.getPosition());

        try {
            // 🚨 VALIDACIÓN CRÍTICA: Verificar que targetProcessId no sea null
            if (request.getTargetProcessId() == null) {
                log.error("❌ targetProcessId is null in request");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "targetProcessId es requerido"));
            }

            TaskDTO.Response task = taskService.moveTask(id, request, currentUser.getId());

            log.info("✅ Task moved successfully to process {}", request.getTargetProcessId());
            return ResponseEntity.ok(task);

        } catch (IllegalArgumentException e) {
            log.error("❌ IllegalArgumentException en moveTask: {}", e.getMessage());

            // 🔥 DEVOLVER UN OBJETO JSON SIMPLE, NO UN DTO
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now().toString()
                    ));

        } catch (ResourceNotFoundException e) {
            log.error("❌ ResourceNotFoundException en moveTask: {}", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now().toString()
                    ));

        } catch (AccessDeniedException e) {
            log.error("❌ AccessDeniedException en moveTask: {}", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "No tienes permisos para mover esta tarea",
                            "timestamp", LocalDateTime.now().toString()
                    ));

        } catch (Exception e) {
            log.error("❌ Exception inesperada en moveTask: {}", e.getMessage(), e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error interno del servidor",
                            "message", e.getMessage(),
                            "timestamp", LocalDateTime.now().toString()
                    ));
        }
    }
    /**
     * Asigna un usuario a una tarea
     * POST /api/projects/{projectId}/tasks/{taskId}/assignees/{userId}
     */
    @PostMapping("/{taskId}/assignees/{userId}")
    public ResponseEntity<TaskDTO.Response> assignUser(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/projects/{}/tasks/{}/assignees/{}",
                projectId, taskId, userId);

        try {
            TaskDTO.Response task = taskService.assignUser(
                    taskId,
                    userId,
                    currentUser.getId()
            );

            return ResponseEntity.ok(task);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * Desasigna un usuario de una tarea
     * DELETE /api/projects/{projectId}/tasks/{taskId}/assignees/{userId}
     */
    @DeleteMapping("/{taskId}/assignees/{userId}")
    public ResponseEntity<TaskDTO.Response> unassignUser(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/projects/{}/tasks/{}/assignees/{}",
                projectId, taskId, userId);

        try {
            TaskDTO.Response task = taskService.unassignUser(
                    taskId,
                    userId,
                    currentUser.getId()
            );

            return ResponseEntity.ok(task);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    @GetMapping("/{id}/subtasks")
    public ResponseEntity<List<TaskDTO.Summary>> getSubtasks(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        log.info("GET /api/projects/{}/tasks/{}/subtasks", projectId, id);

        try {
            // 1️⃣ obtener la task para verificar el proyecto
            TaskDTO.Response parentTask = taskService.getTaskById(id, currentUser.getId());

            // 2️⃣ validar que coincida el proyecto del path
            if (!parentTask.getProjectId().equals(projectId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 3️⃣ obtener subtareas
            List<TaskDTO.Summary> subtasks = taskService.getSubtasks(
                    id,
                    currentUser.getId()
            );

            return ResponseEntity.ok(subtasks);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    /**
     * N°13: Busca tareas por palabra clave
     * GET /api/projects/{projectId}/tasks/search?keyword=texto
     *
     * Usa Query Method: searchByProjectAndKeyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<TaskDTO.Response>> searchTasks(
            @PathVariable Long projectId,
            @RequestParam String keyword,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/tasks/search?keyword={}", projectId, keyword);

        try {
            List<TaskDTO.Response> tasks = taskService.searchTasks(
                    projectId,
                    keyword,
                    currentUser.getId()
            );

            return ResponseEntity.ok(tasks);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
    }

    /**
     * N°17: Obtiene tareas con fecha límite próxima
     * GET /api/projects/{projectId}/tasks/upcoming?days=7
     *
     * Usa Query Method: findByDueDateBetween
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<TaskDTO.Response>> getUpcomingTasks(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "7") int days,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/tasks/upcoming?days={}", projectId, days);

        try {
            List<TaskDTO.Response> tasks = taskService.getUpcomingTasks(
                    projectId,
                    days,
                    currentUser.getId()
            );

            return ResponseEntity.ok(tasks);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
    }

    /**
     * N°17: Obtiene tareas vencidas (overdue)
     * GET /api/projects/{projectId}/tasks/overdue
     *
     * Usa Query Method: findOverdueTasks
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskDTO.Response>> getOverdueTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/tasks/overdue", projectId);

        try {
            List<TaskDTO.Response> tasks = taskService.getOverdueTasks(
                    projectId,
                    currentUser.getId()
            );

            return ResponseEntity.ok(tasks);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
    }

    /**
     * Elimina una tarea
     * DELETE /api/projects/{projectId}/tasks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/projects/{}/tasks/{}", projectId, id);

        try {
            taskService.deleteTask(id, currentUser.getId());
            return ResponseEntity.noContent().build();

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