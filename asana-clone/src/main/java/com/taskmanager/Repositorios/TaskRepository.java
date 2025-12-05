package com.taskmanager.Repositorios;

import com.taskmanager.model.Task;
import com.taskmanager.model.Task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // ===================================================================
    // 📊 MÉTODOS DE MANIPULACIÓN DE POSICIÓN (KANBAN)
    // ===================================================================

    /**
     * Desplaza las posiciones de las tareas en un proceso (columna)
     * a partir de una posición inicial. Usado para inserción o eliminación.
     * Ej: shift = 1 (abrir hueco), shift = -1 (cerrar hueco)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.position = t.position + :shift " +
            "WHERE t.process.id = :processId AND t.position >= :startPosition")
    void shiftPositions(
            @Param("processId") Long processId,
            @Param("startPosition") int startPosition,
            @Param("shift") int shift);
    // 🔥 Nuevo método para carga explícita (Eager) de las tareas para exportación


    // ✅ Carga todas las tareas del proyecto y, de forma inmediata (Eager),
    //    sus asignados (assignees) y su tarea padre (parentTask).
    @Query("SELECT DISTINCT t FROM Task t " +
            "LEFT JOIN FETCH t.assignees a " + // Para task.getAssignees().stream() en generateCSV/PDF
            "LEFT JOIN FETCH t.parentTask pt " + // Para task.getParentTask() en generatePDF
            "WHERE t.project.id = :projectId " +
            "ORDER BY t.createdAt DESC")
    List<Task> findByProjectIdForExport(@Param("projectId") Long projectId);

    /**
     * Desplaza las posiciones de las tareas dentro de un rango específico
     * y en el mismo proceso (columna). Usado para movimientos internos de arrastrar y soltar.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.position = t.position + :shift " +
            "WHERE t.process.id = :processId " +
            "AND t.position >= :startPosition AND t.position <= :endPosition")
    void shiftPositionsInSameProcess(
            @Param("processId") Long processId,
            @Param("startPosition") int startPosition,
            @Param("endPosition") int endPosition,
            @Param("shift") int shift);

    // ===================================================================
    // 👥 MÉTODOS DE LECTURA DE ACCESO Y ASIGNACIÓN (EFICIENTE)
    // ===================================================================

    /**
     * Obtiene todas las tareas asignadas a un usuario específico,
     * cargando de manera eficiente el Proyecto y el Proceso asociado.
     */
    @Query("SELECT t FROM Task t " +
            "JOIN FETCH t.project p " +
            "JOIN FETCH t.process proc " +
            "JOIN t.assignees a WHERE a.id = :userId")
    List<Task> findTasksAssignedToUserWithProject(@Param("userId") Long userId);

    /**
     * Obtiene tareas accesibles por un usuario en un proyecto.
     * (Creador de la tarea, Creador del proyecto, o Miembro del proyecto).
     * NOTA: Este método es principalmente para validación de acceso si el usuario
     * no es 'ADMIN', pero en el `TaskService` consolidado, se recomienda usar
     * `findByProjectId` después de una comprobación de acceso general.
     */
    @Query("SELECT t FROM Task t JOIN t.project project " +
            "LEFT JOIN project.members member " +
            "WHERE t.project.id = :projectId " +
            "AND (t.createdBy.id = :userId " +
            "     OR project.createdBy.id = :userId " +
            "     OR member.id = :userId)")
    List<Task> findTasksByProjectAndUserAccess(
            @Param("projectId") Long projectId,
            @Param("userId") Long userId
    );

    /**
     * Obtiene tareas asignadas a un usuario (consulta simple)
     */
    @Query("SELECT t FROM Task t JOIN t.assignees a WHERE a.id = :userId")
    List<Task> findByAssigneeId(@Param("userId") Long userId);

    // ===================================================================
    // 📚 CONSULTAS BASADAS EN JERARQUÍA Y AGRUPACIÓN
    // ===================================================================

    /**
     * Encuentra todas las tareas de un proyecto específico
     * (Atraviesa la relación Task -> Process -> Project)
     */
    @Query("SELECT t FROM Task t WHERE t.process.project.id = :projectId")
    List<Task> findByProjectId(@Param("projectId") Long projectId);

    /**
     * Encuentra tareas ordenadas por posición dentro de un proceso (columna Kanban).
     */
    List<Task> findByProcessIdOrderByPositionAsc(Long processId);

    /**
     * Cuenta el número de tareas en un proceso (columna).
     */
    long countByProcessId(Long processId);

    /**
     * Encuentra subtareas por ID de la tarea padre.
     */
    List<Task> findByParentTaskId(Long parentTaskId);

    /**
     * Encuentra las tareas que son tareas principales (no subtareas).
     */
    List<Task> findByParentTaskIsNull();

    // ===================================================================
    // 🔍 CONSULTAS DE BÚSQUEDA Y FILTRO
    // ===================================================================

    /**
     * Búsqueda por palabra clave en título o descripción, limitada a un proyecto.
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> searchByProjectAndKeyword(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword
    );

    /**
     * Búsqueda por rango de fechas de vencimiento.
     */
    List<Task> findByDueDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Encuentra tareas vencidas que no están en estado 'DONE' o 'CANCELLED'.
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate < :now " +
            "AND t.status != :doneStatus " +
            "AND t.status != :cancelledStatus")
    List<Task> findOverdueTasks(
            @Param("now") LocalDateTime now,
            @Param("doneStatus") TaskStatus doneStatus,
            @Param("cancelledStatus") TaskStatus cancelledStatus
    );

    /**
     * Encuentra tareas por estado en un proyecto específico.
     */
    @Query("SELECT t FROM Task t WHERE t.process.project.id = :projectId AND t.status = :status")
    List<Task> findByProjectIdAndStatus(
            @Param("projectId") Long projectId,
            @Param("status") TaskStatus status
    );
    List<Task> findByParentTask(Task parentTask);
    List<Task> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    void deleteAllByProjectId(Long projectId);
    // ===================================================================
    // 📈 MÉTODOS DE CONTEO Y ESTADÍSTICAS
    // ===================================================================

    long countByProjectIdAndStatus(Long projectId, TaskStatus status);
    long countByParentTaskId(Long parentTaskId);

    @Query("SELECT COUNT(t) FROM Task t JOIN t.assignees a " +
            "WHERE a.id = :userId AND t.status = :status")
    long countByAssigneeIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status
    );
}