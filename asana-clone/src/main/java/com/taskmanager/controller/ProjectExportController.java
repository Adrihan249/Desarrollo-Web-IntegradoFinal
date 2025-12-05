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
// PROJECT EXPORT CONTROLLER (Extension)
// ===================================
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Slf4j
public class ProjectExportController {

    private final ExportService exportService;

    /**
     * Exportación rápida de tareas
     */
    @GetMapping("/export")
    public ResponseEntity<ExportDTO.Response> exportTasks(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "CSV") ExportFormat format
    ) {
        log.info("GET /api/projects/{}/tasks/export - User: {}, Format: {}",
                projectId, currentUser.getEmail(), format);

        ExportDTO.CreateRequest request = ExportDTO.CreateRequest.builder()
                .type(ExportType.TASKS_ONLY)
                .format(format)
                .referenceId(projectId)
                .build();

        ExportDTO.Response export = exportService
                .requestExport(currentUser.getId(), request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(export);
    }
}