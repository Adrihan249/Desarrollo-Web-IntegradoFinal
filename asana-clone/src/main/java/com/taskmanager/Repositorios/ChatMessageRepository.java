package com.taskmanager.Repositorios;

import com.taskmanager.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
/**
 * ===================================================================
 * ChatMessageRepository - Repositorio de Mensajes de Chat (N°14)
 *
 * Query Methods sin SQL para chat del proyecto
 * ===================================================================
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * N°14: Busca mensajes de un proyecto ordenados por fecha
     * Query Method: SELECT * FROM chat_messages WHERE project_id = ? ORDER BY created_at ASC
     */
    List<ChatMessage> findByProjectIdOrderByCreatedAtAsc(Long projectId);

    /**
     * N°14: Busca mensajes de un proyecto (con paginación)
     */
    Page<ChatMessage> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    /**
     * N°14: Busca mensajes no eliminados de un proyecto
     */
    List<ChatMessage> findByProjectIdAndDeletedFalseOrderByCreatedAtAsc(Long projectId);

    /**
     * N°14: Busca mensajes enviados por un usuario
     */
    List<ChatMessage> findBySenderId(Long senderId);

    /**
     * N°14: Busca respuestas a un mensaje
     * Query Method: SELECT * FROM chat_messages WHERE parent_message_id = ?
     */
    List<ChatMessage> findByParentMessageIdOrderByCreatedAtAsc(Long parentMessageId);

    /**
     * N°14: Cuenta respuestas a un mensaje
     */
    long countByParentMessageId(Long parentMessageId);

    /**
     * N°14: Busca mensajes fijados de un proyecto
     */
    List<ChatMessage> findByProjectIdAndPinnedTrueOrderByPinnedAtDesc(Long projectId);

    /**
     * N°14: Busca mensajes por tipo
     */
    List<ChatMessage> findByProjectIdAndType(Long projectId, ChatMessage.MessageType type);

    /**
     * N°14: Busca mensajes donde se menciona a un usuario
     */
    @Query("SELECT cm FROM ChatMessage cm JOIN cm.mentionedUserIds m WHERE m = :userId")
    List<ChatMessage> findByMentionedUserId(@Param("userId") Long userId);

    /**
     * N°14: Busca mensajes recientes de un proyecto
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.project.id = :projectId " +
            "AND cm.createdAt >= :since AND cm.deleted = false " +
            "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findRecentMessages(
            @Param("projectId") Long projectId,
            @Param("since") LocalDateTime since
    );

    /**
     * N°14: Busca mensajes no leídos por un usuario
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.project.id = :projectId " +
            "AND :userId NOT MEMBER OF cm.readByUserIds " +
            "AND cm.sender.id != :userId " +
            "AND cm.deleted = false " +
            "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findUnreadByUserInProject(
            @Param("projectId") Long projectId,
            @Param("userId") Long userId
    );

    /**
     * N°14: Cuenta mensajes no leídos
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.project.id = :projectId " +
            "AND :userId NOT MEMBER OF cm.readByUserIds " +
            "AND cm.sender.id != :userId " +
            "AND cm.deleted = false")
    long countUnreadByUserInProject(
            @Param("projectId") Long projectId,
            @Param("userId") Long userId
    );

    /**
     * N°13: Busca mensajes por palabra clave
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.project.id = :projectId " +
            "AND LOWER(cm.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND cm.deleted = false " +
            "ORDER BY cm.createdAt DESC")
    List<ChatMessage> searchMessages(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword
    );
}

