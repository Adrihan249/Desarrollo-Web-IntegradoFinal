package com.taskmanager.controller;

import com.taskmanager.dto.*;
import com.taskmanager.model.User;
import com.taskmanager.model.enums.*;
import com.taskmanager.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * ✅ VERSIÓN SIMPLE - Usa User directamente
 */
@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
@Slf4j
public class ExportController {

    private final ExportService exportService;

    /**
     * ✅ SIMPLIFICADO: Solicitar exportación de proyecto
     * POST /api/exports/project/{projectId}
     */
    @PostMapping("/project/{projectId}")
    public ResponseEntity<ExportDTO.Response> requestExport(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long projectId,
            @Valid @RequestBody ExportDTO.CreateRequest request
    ) {
        log.info("📦 Export request - Project: {}, User: {}", projectId,
                currentUser != null ? currentUser.getEmail() : "NULL");

        // Asegurar que referenceId sea el projectId
        request.setReferenceId(projectId);

        ExportDTO.Response export = exportService
                .requestExport(currentUser.getId(), request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(export);
    }

    /**
     * ✅ SIMPLIFICADO: Obtener mis exportaciones
     */
    @GetMapping("/my-exports")
    public ResponseEntity<List<ExportDTO.Response>> getMyExports(
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("📋 Get exports - User: {}", currentUser.getEmail());

        List<ExportDTO.Response> exports = exportService
                .getUserExports(currentUser.getId());

        return ResponseEntity.ok(exports);
    }

    /**
     * ✅ SIMPLIFICADO: Descargar archivo
     */
    @GetMapping("/{jobId}/download")
    public ResponseEntity<byte[]> downloadExport(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long jobId
    ) throws IOException {
        log.info("⬇️ Download export {} - User: {}", jobId, currentUser.getEmail());

        ExportDTO.Response export = exportService
                .getExportById(jobId, currentUser.getId());

        byte[] fileData = exportService
                .downloadExport(jobId, currentUser.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(export.getFileName())
                        .build()
        );

        return ResponseEntity.ok().headers(headers).body(fileData);
    }
}