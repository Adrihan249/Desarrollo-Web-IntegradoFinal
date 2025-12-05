package com.taskmanager.controller;

import com.taskmanager.dto.*;
import com.taskmanager.model.User;
import com.taskmanager.service.ChatMessageService;
import com.taskmanager.service.ActivityLogService;
import com.taskmanager.service.FilterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
/**
        * ===================================================================
        * Controlador REST de Filtros Avanzados
 *
         * CUMPLE REQUERIMIENTO N°13: Filtros y búsqueda avanzada
 * ===================================================================
         */
@RestController
@RequestMapping("/api/projects/{projectId}/tasks/filter")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
class FilterController {

    private final FilterService filterService;

    /**
     * N°13: Filtra tareas con múltiples criterios
     * POST /api/projects/{projectId}/tasks/filter
     */
    @PostMapping
    public ResponseEntity<Page<TaskDTO.Response>> filterTasks(
            @PathVariable Long projectId,
            @RequestBody FilterDTO.TaskFilterRequest filter,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/projects/{}/tasks/filter", projectId);

        try {
            Page<TaskDTO.Response> tasks = filterService.filterTasks(
                    projectId, filter, currentUser.getId()
            );

            return ResponseEntity.ok(tasks);

        } catch (Exception e) {
            log.error("Error filtering tasks: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }
}
