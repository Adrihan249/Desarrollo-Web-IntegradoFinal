package com.taskmanager.service;

import com.taskmanager.Repositorios.CommentRepository;
import com.taskmanager.Repositorios.TaskRepository;
import com.taskmanager.Repositorios.UserRepository;
import com.taskmanager.dto.CommentDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.CommentMapper;
import com.taskmanager.model.Comment;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    /**
     * Crear un comentario en una tarea
     */
    public CommentDTO.Response createComment(Long taskId, CommentDTO.CreateRequest request, Long userId) {
        log.info("Creating comment on task ID: {} by user ID: {}", taskId, userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        // Validar que el usuario sea asignado o creador
        boolean hasAccess = task.getCreatedBy().getId().equals(userId) ||
                task.getAssignees().stream().anyMatch(a -> a.getId().equals(userId)) ||
                task.getProject().getMembers().stream().anyMatch(m -> m.getId().equals(userId));

        if (!hasAccess) {
            throw new AccessDeniedException("No tienes acceso para comentar en esta tarea");
        }

        Comment comment = Comment.builder()
                .task(task)
                .author(user)
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);
        log.info("Comment created successfully with ID: {}", saved.getId());

        return commentMapper.toResponse(saved);
    }

    /**
     * Obtener comentarios de una tarea
     */
    @Transactional(readOnly = true)
    public List<CommentDTO.Response> getTaskComments(Long taskId, Long userId) {
        log.debug("Fetching comments for task ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + taskId));

        // Validar acceso
        boolean hasAccess = task.getCreatedBy().getId().equals(userId) ||
                task.getAssignees().stream().anyMatch(a -> a.getId().equals(userId)) ||
                task.getProject().getMembers().stream().anyMatch(m -> m.getId().equals(userId));

        if (!hasAccess) {
            throw new AccessDeniedException("No tienes acceso a los comentarios de esta tarea");
        }

        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Eliminar un comentario
     */
    public void deleteComment(Long commentId, Long userId) {
        log.info("Deleting comment ID: {} by user ID: {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con ID: " + commentId));

        // Solo el autor puede eliminar
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("Solo el autor puede eliminar este comentario");
        }

        commentRepository.delete(comment);
        log.info("Comment deleted successfully");
    }
}