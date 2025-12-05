package com.taskmanager.controller;

import com.taskmanager.dto.ProcessDTO;
import com.taskmanager.model.User;
import com.taskmanager.service.ProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST de Procesos (Columnas Kanban)
 *
 * CUMPLE REQUERIMIENTO N°5: Gestión de procesos
 *
 * Endpoints para gestionar columnas del tablero Kanban
 */
@RestController
@RequestMapping("/api/projects/{projectId}/processes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ProcessController {

    private final ProcessService processService;

    /**
     * N°5: Crea un nuevo proceso (columna Kanban)
     * POST /api/projects/{projectId}/processes
     *
     * @param projectId ID del proyecto
     * @param request Datos del proceso
     * @param currentUser Usuario autenticado
     * @return ResponseEntity con proceso creado
     *
     * Status codes:
     * - 201 CREATED: Proceso creado exitosamente
     * - 400 BAD REQUEST: Datos inválidos
     * - 403 FORBIDDEN: No tiene permisos
     * - 404 NOT FOUND: Proyecto no existe
     */
    @PostMapping
    public ResponseEntity<ProcessDTO.Response> createProcess(
            @PathVariable Long projectId,
            @Valid @RequestBody ProcessDTO.CreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/projects/{}/processes - Creating process: {}",
                projectId, request.getName());

        try {
            ProcessDTO.Response process = processService.createProcess(
                    projectId,
                    request,
                    currentUser.getId()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(process);

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
            log.error("Error creating process: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°5: Obtiene todos los procesos de un proyecto
     * GET /api/projects/{projectId}/processes
     *
     * Ordenados por posición para el tablero Kanban
     * Usa Query Method: findByProjectIdOrderByPositionAsc
     *
     * @param projectId ID del proyecto
     * @param currentUser Usuario autenticado
     * @return ResponseEntity con lista de procesos
     *
     * Status codes:
     * - 200 OK: Lista retornada
     * - 403 FORBIDDEN: No tiene acceso al proyecto
     * - 404 NOT FOUND: Proyecto no existe
     */
    @GetMapping
    public ResponseEntity<List<ProcessDTO.Response>> getProjectProcesses(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/processes", projectId);

        try {
            List<ProcessDTO.Response> processes = processService.getProjectProcesses(
                    projectId,
                    currentUser.getId()
            );

            return ResponseEntity.ok(processes);

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
     * N°5: Obtiene un proceso por ID
     * GET /api/projects/{projectId}/processes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProcessDTO.Response> getProcessById(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/processes/{}", projectId, id);

        try {
            ProcessDTO.Response process = processService.getProcessById(
                    id,
                    currentUser.getId()
            );

            return ResponseEntity.ok(process);

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
     * N°5: Actualiza un proceso
     * PUT /api/projects/{projectId}/processes/{id}
     *
     * Solo el creador del proyecto puede actualizar
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProcessDTO.Response> updateProcess(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody ProcessDTO.UpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/projects/{}/processes/{}", projectId, id);

        try {
            ProcessDTO.Response process = processService.updateProcess(
                    id,
                    request,
                    currentUser.getId()
            );

            return ResponseEntity.ok(process);

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
     * N°5: Reordena procesos en el tablero
     * POST /api/projects/{projectId}/processes/{id}/reorder
     *
     * @param projectId ID del proyecto
     * @param id ID del proceso a mover
     * @param request Nueva posición
     */
    @PostMapping("/{id}/reorder")
    public ResponseEntity<List<ProcessDTO.Response>> reorderProcess(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @RequestBody ProcessDTO.ReorderRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/projects/{}/processes/{}/reorder to position {}",
                projectId, id, request.getNewPosition());

        try {
            List<ProcessDTO.Response> processes = processService.reorderProcesses(
                    projectId,
                    id,
                    request.getNewPosition(),
                    currentUser.getId()
            );

            return ResponseEntity.ok(processes);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    /**
     * N°5: Elimina un proceso
     * DELETE /api/projects/{projectId}/processes/{id}
     *
     * Solo se puede eliminar si está vacío
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProcess(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/projects/{}/processes/{}", projectId, id);

        try {
            processService.deleteProcess(id, currentUser.getId());
            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            // Proceso no está vacío
            log.error("Cannot delete process: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();

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
     * N°5: Crea procesos por defecto
     * POST /api/projects/{projectId}/processes/defaults
     *
     * Útil al crear un proyecto nuevo
     */
    @PostMapping("/defaults")
    public ResponseEntity<List<ProcessDTO.Response>> createDefaultProcesses(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/projects/{}/processes/defaults", projectId);

        try {
            List<ProcessDTO.Response> processes = processService.createDefaultProcesses(
                    projectId,
                    currentUser.getId()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(processes);

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