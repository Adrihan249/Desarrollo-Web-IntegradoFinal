package com.taskmanager.controller;

import com.taskmanager.dto.CommentDTO;
import com.taskmanager.model.User;
import com.taskmanager.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class CommentController {

    private final CommentService commentService;

    /**
     * Crear comentario
     * POST /api/tasks/{taskId}/comments
     */
    @PostMapping
    public ResponseEntity<CommentDTO.Response> createComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentDTO.CreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/tasks/{}/comments - Creating comment", taskId);

        try {
            CommentDTO.Response comment = commentService.createComment(
                    taskId,
                    request,
                    currentUser.getId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (Exception e) {
            log.error("Error creating comment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Obtener comentarios de una tarea
     * GET /api/tasks/{taskId}/comments
     */
    @GetMapping
    public ResponseEntity<List<CommentDTO.Response>> getTaskComments(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/tasks/{}/comments", taskId);

        try {
            List<CommentDTO.Response> comments = commentService.getTaskComments(
                    taskId,
                    currentUser.getId()
            );
            return ResponseEntity.ok(comments);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Eliminar comentario
     * DELETE /api/tasks/{taskId}/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/tasks/{}/comments/{}", taskId, commentId);

        try {
            commentService.deleteComment(commentId, currentUser.getId());
            return ResponseEntity.noContent().build();

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}