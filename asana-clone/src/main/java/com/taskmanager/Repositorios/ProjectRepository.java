package com.taskmanager.Repositorios;

import com.taskmanager.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByCreatedById(Long userId);

    int countByCreatedById(Long userId);

    /**
     * 🔥 QUERY CORREGIDA PARA EXPORTACIÓN
     * Carga EAGER todas las relaciones necesarias
     */
    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.createdBy " +
            "LEFT JOIN FETCH p.members m " +
            "LEFT JOIN FETCH p.processes proc " +
            "WHERE p.id = :projectId")
    Optional<Project> findByIdForExport(@Param("projectId") Long projectId);

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :userId")
    List<Project> findByMemberId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN p.members m " +
            "WHERE p.createdBy.id = :userId OR m.id = :userId")
    List<Project> findAllByUserId(@Param("userId") Long userId);

    List<Project> findByNameContainingIgnoreCase(String name);

    List<Project> findByStatus(Project.ProjectStatus status);

    List<Project> findByArchivedFalse();

    @Query("""
    SELECT p FROM Project p
    LEFT JOIN p.members m
    WHERE 
        (LOWER(p.name) LIKE :query OR LOWER(p.description) LIKE :query)
        AND (p.createdBy.id = :userId OR m.id = :userId)
    """)
    List<Project> searchByNameOrDescription(
            @Param("query") String query,
            @Param("userId") Long userId
    );

    @Query("""
    SELECT p FROM Project p
    LEFT JOIN p.members m
    WHERE p.status = :status
      AND (p.createdBy.id = :userId OR m.id = :userId)
    """)
    List<Project> findByStatusAndUser(
            @Param("status") Project.ProjectStatus status,
            @Param("userId") Long userId
    );

    @Query("""
    SELECT p FROM Project p
    LEFT JOIN p.members m
    WHERE (p.createdBy.id = :userId OR m.id = :userId)
      AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    List<Project> searchProjects(@Param("query") String query, @Param("userId") Long userId);

    @Query("SELECT p FROM Project p WHERE " +
            "p.deadline BETWEEN :startDate AND :endDate " +
            "AND p.archived = false " +
            "ORDER BY p.deadline ASC")
    List<Project> findProjectsWithUpcomingDeadline(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    int countByCreatedByIdAndArchivedFalse(Long userId);

    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN p.members m " +
            "WHERE (:userId IS NULL OR p.createdBy.id = :userId OR m.id = :userId) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:archived IS NULL OR p.archived = :archived) " +
            "AND (:keyword IS NULL OR " +
            "     LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Project> searchProjects(
            @Param("userId") Long userId,
            @Param("status") Project.ProjectStatus status,
            @Param("archived") Boolean archived,
            @Param("keyword") String keyword
    );
}