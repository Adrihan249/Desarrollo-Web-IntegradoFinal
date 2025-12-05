package com.taskmanager.Repositorios;

import com.taskmanager.model.*;
import com.taskmanager.model.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ===================================================================
 * ProcessRepository - Repositorio de Procesos (N°5)
 *
 * Query Methods sin SQL para gestión de columnas Kanban
 * ===================================================================
 */
@Repository
public interface ProcessRepository extends JpaRepository<Process, Long> {

    /**
     * N°5: Busca procesos de un proyecto ordenados por posición
     * Query Method: SELECT * FROM processes WHERE project_id = ? ORDER BY position ASC
     */
    List<Process> findByProjectIdOrderByPositionAsc(Long projectId);

    /**
     * N°5: Busca procesos de un proyecto
     */
    List<Process> findByProjectId(Long projectId);

    /**
     * N°5: Cuenta procesos de un proyecto
     */
    long countByProjectId(Long projectId);

    /**
     * N°5: Busca proceso por proyecto y posición
     */
    Process findByProjectIdAndPosition(Long projectId, Integer position);

    /**
     * N°5: Busca procesos que marcan tareas como completadas
     */
    List<Process> findByProjectIdAndIsCompletedTrue(Long projectId);
}