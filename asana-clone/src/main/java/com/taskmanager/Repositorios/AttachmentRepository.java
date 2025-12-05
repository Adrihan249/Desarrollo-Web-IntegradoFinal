package com.taskmanager.Repositorios;

import com.taskmanager.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
/**
 * ===================================================================
 * AttachmentRepository - Repositorio de Adjuntos (N°11)
 *
 * Query Methods sin SQL para gestión de archivos adjuntos
 * ===================================================================
 */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    /**
     * N°11: Busca adjuntos de una tarea ordenados por fecha
     * Query Method: SELECT * FROM attachments WHERE task_id = ? ORDER BY created_at DESC
     */
    List<Attachment> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    /**
     * N°11: Busca adjuntos de una tarea
     */
    List<Attachment> findByTaskId(Long taskId);

    /**
     * N°11: Cuenta adjuntos de una tarea
     */
    long countByTaskId(Long taskId);

    /**
     * N°11: Busca adjuntos subidos por un usuario
     */
    List<Attachment> findByUploadedById(Long userId);

    /**
     * N°11: Busca imágenes de una tarea
     * Query Method: SELECT * FROM attachments WHERE task_id = ? AND is_image = true
     */
    List<Attachment> findByTaskIdAndIsImageTrue(Long taskId);

    /**
     * N°11: Busca adjuntos por tipo MIME
     */
    List<Attachment> findByMimeType(String mimeType);

    /**
     * N°11: Busca adjuntos por extensión
     */
    List<Attachment> findByFileExtension(String extension);

    /**
     * N°11: Busca adjuntos por nombre
     */
    List<Attachment> findByFileNameContainingIgnoreCase(String fileName);

    /**
     * Estadísticas: Suma del tamaño total de archivos de una tarea
     */
    @Query("SELECT SUM(a.fileSize) FROM Attachment a WHERE a.task.id = :taskId")
    Long sumFileSizeByTaskId(@Param("taskId") Long taskId);

    /**
     * Estadísticas: Total de descargas de archivos de un proyecto
     */
    @Query("SELECT SUM(a.downloadCount) FROM Attachment a WHERE a.task.project.id = :projectId")
    Long sumDownloadCountByProjectId(@Param("projectId") Long projectId);
}