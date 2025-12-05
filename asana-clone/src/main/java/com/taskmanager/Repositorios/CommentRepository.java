package com.taskmanager.Repositorios;

import com.taskmanager.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Obtiene todos los comentarios de una tarea, ordenados por fecha (más reciente primero)
     */
    List<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    /**
     * Cuenta el número de comentarios de una tarea
     */
    long countByTaskId(Long taskId);
}