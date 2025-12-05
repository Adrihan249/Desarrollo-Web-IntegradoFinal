package com.taskmanager.Repositorios;

import com.taskmanager.model.*;
import com.taskmanager.model.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
// ===================================
// EXPORT JOB REPOSITORY
// ===================================
@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {

    // Exports de un usuario
    @Query("SELECT j FROM ExportJob j JOIN FETCH j.requestedBy WHERE j.requestedBy.id = :userId ORDER BY j.createdAt DESC")
    List<ExportJob> findByRequestedByIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    // Exports por estado
    List<ExportJob> findByStatusOrderByCreatedAtDesc(ExportStatus status);

    // Exports completados y disponibles
    @Query("SELECT e FROM ExportJob e WHERE " +
            "e.status = 'DONE' AND " +
            "e.expiresAt > :now " +
            "ORDER BY e.createdAt DESC")
    List<ExportJob> findAvailableExports(@Param("now") LocalDateTime now);

    // Exports expirados para limpiar
    @Query("SELECT e FROM ExportJob e WHERE " +
            "e.status = 'COMPLETED' AND " +
            "e.expiresAt < :now")
    List<ExportJob> findExpiredExports(@Param("now") LocalDateTime now);

    // Contar exports pendientes
    long countByStatusIn(List<ExportStatus> statuses);

    // Exports de un usuario por estado
    List<ExportJob> findByRequestedByIdAndStatus(Long userId, ExportStatus status);

    // Exports por tipo
    List<ExportJob> findByTypeOrderByCreatedAtDesc(ExportType type);

    // Exports en proceso o pendientes
    @Query("SELECT e FROM ExportJob e WHERE " +
            "e.status IN ('PENDING', 'PROCESSING') " +
            "ORDER BY e.createdAt ASC")
    List<ExportJob> findActiveExports();

    // Último export exitoso de un usuario
    @Query("SELECT e FROM ExportJob e WHERE " +
            "e.requestedBy.id = :userId AND " +
            "e.status = 'COMPLETED' " +
            "ORDER BY e.completedAt DESC " +
            "LIMIT 1")
    Optional<ExportJob> findLastSuccessfulExport(@Param("userId") Long userId);

    // Exports por referencia (ej: proyecto específico)
    List<ExportJob> findByReferenceIdAndTypeOrderByCreatedAtDesc(
            Long referenceId,
            ExportType type
    );

    // Tamaño total de archivos por usuario
    @Query("SELECT COALESCE(SUM(e.fileSize), 0) FROM ExportJob e WHERE " +
            "e.requestedBy.id = :userId AND " +
            "e.status = 'COMPLETED'")
    Long getTotalFileSizeByUser(@Param("userId") Long userId);

    // Contar exports completados por usuario
    long countByRequestedByIdAndStatus(Long userId, ExportStatus status);
}