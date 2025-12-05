package com.taskmanager.controller;

import com.taskmanager.dto.AttachmentDTO;
import com.taskmanager.dto.CommentDTO;
import com.taskmanager.model.User;
import com.taskmanager.service.AttachmentService;
import com.taskmanager.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * ===================================================================
 * Controlador REST de Archivos Adjuntos
 *
 * CUMPLE REQUERIMIENTO N°11: Adjuntar archivos
 * ===================================================================
 */
@RestController
@RequestMapping("/api/tasks/{taskId}/attachments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * N°11: Sube un archivo a una tarea
     * POST /api/tasks/{taskId}/attachments
     *
     * Content-Type: multipart/form-data
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentDTO.Response> uploadFile(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/tasks/{}/attachments - Uploading file: {}",
                taskId, file.getOriginalFilename());

        try {
            AttachmentDTO.Response attachment = attachmentService.uploadFile(
                    taskId,
                    file,
                    description,
                    currentUser.getId()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(attachment);

        } catch (IllegalArgumentException e) {
            log.error("Invalid file: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°11: Obtiene archivos adjuntos de una tarea
     * GET /api/tasks/{taskId}/attachments
     *
     * Usa Query Method: findByTaskIdOrderByCreatedAtDesc
     */
    @GetMapping
    public ResponseEntity<List<AttachmentDTO.Response>> getTaskAttachments(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/tasks/{}/attachments", taskId);

        try {
            List<AttachmentDTO.Response> attachments = attachmentService.getTaskAttachments(
                    taskId,
                    currentUser.getId()
            );

            return ResponseEntity.ok(attachments);

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
     * N°11: Obtiene un archivo adjunto por ID
     * GET /api/tasks/{taskId}/attachments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AttachmentDTO.Response> getAttachmentById(
            @PathVariable Long taskId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/tasks/{}/attachments/{}", taskId, id);

        try {
            AttachmentDTO.Response attachment = attachmentService.getAttachmentById(
                    id,
                    currentUser.getId()
            );

            return ResponseEntity.ok(attachment);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°11: Actualiza descripción de un archivo
     * PUT /api/tasks/{taskId}/attachments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AttachmentDTO.Response> updateAttachment(
            @PathVariable Long taskId,
            @PathVariable Long id,
            @RequestParam String description,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/tasks/{}/attachments/{}", taskId, id);

        try {
            AttachmentDTO.Response attachment = attachmentService.updateAttachment(
                    id,
                    description,
                    currentUser.getId()
            );

            return ResponseEntity.ok(attachment);

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
     * N°11: Elimina un archivo
     * DELETE /api/tasks/{taskId}/attachments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long taskId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/tasks/{}/attachments/{}", taskId, id);

        try {
            attachmentService.deleteAttachment(id, currentUser.getId());
            return ResponseEntity.noContent().build();

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }
}

/**
 * ===================================================================
 * Controlador para descarga de archivos
 * Ruta separada para mejor organización
 * ===================================================================
 */
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
class AttachmentDownloadController {

    private final AttachmentService attachmentService;

    /**
     * N°11: Descarga un archivo
     * GET /api/attachments/{id}/download
     *
     * Retorna el archivo como Resource para descarga
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/attachments/{}/download", id);

        try {
            // Obtiene metadata del archivo
            AttachmentDTO.Response attachment = attachmentService.getAttachmentById(
                    id,
                    currentUser.getId()
            );

            // Descarga el archivo
            Resource resource = attachmentService.downloadFile(id, currentUser.getId());

            // Configura headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + attachment.getFileName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, attachment.getMimeType());

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(resource);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (IOException e) {
            log.error("Error downloading file: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }
}
