package com.taskmanager.Repositorios;

import com.taskmanager.model.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    /**
     * Verifica si un usuario bloqueó a otro
     */
    @Query("SELECT CASE WHEN COUNT(ub) > 0 THEN true ELSE false END " +
            "FROM UserBlock ub " +
            "WHERE ub.blocker.id = :blockerId " +
            "AND ub.blocked.id = :blockedId")
    boolean existsByBlockerIdAndBlockedId(
            @Param("blockerId") Long blockerId,
            @Param("blockedId") Long blockedId
    );

    /**
     * Busca un bloqueo específico
     */
    Optional<UserBlock> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    /**
     * Obtiene todos los usuarios bloqueados por un usuario
     */
    List<UserBlock> findByBlockerId(Long blockerId);

    /**
     * Obtiene todos los usuarios que han bloqueado a un usuario
     */
    List<UserBlock> findByBlockedId(Long blockedId);

    /**
     * Elimina un bloqueo
     */
    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}