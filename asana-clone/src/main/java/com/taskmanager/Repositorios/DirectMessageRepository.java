package com.taskmanager.Repositorios;

import com.taskmanager.model.DirectMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // Importar Modifying
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    /**
     * Busca mensajes de una conversación
     */
    List<DirectMessage> findByConversationIdOrderByCreatedAtDesc(
            String conversationId,
            Pageable pageable
    );

    /**
     * Busca todas las conversaciones de un usuario
     */
    @Query("SELECT DISTINCT dm.conversationId FROM DirectMessage dm " +
            "WHERE (dm.sender.id = :userId OR dm.receiver.id = :userId) " +
            "AND dm.deleted = false")
    List<String> findConversationIdsByUserId(@Param("userId") Long userId);

    /**
     * Obtiene el último mensaje de una conversación
     */
    Optional<DirectMessage> findFirstByConversationIdOrderByCreatedAtDesc(
            String conversationId
    );

    /**
     * Cuenta mensajes no leídos en una conversación
     */
    @Query("SELECT COUNT(dm) FROM DirectMessage dm " +
            "WHERE dm.conversationId = :conversationId " +
            "AND dm.receiver.id = :userId " +
            "AND dm.isRead = false " +
            "AND dm.deleted = false")
    long countUnreadInConversation(
            @Param("conversationId") String conversationId,
            @Param("userId") Long userId
    );

    /**
     * Cuenta total de mensajes no leídos de un usuario
     */
    @Query("SELECT COUNT(dm) FROM DirectMessage dm " +
            "WHERE dm.receiver.id = :userId " +
            "AND dm.isRead = false " +
            "AND dm.deleted = false")
    long countUnreadByUser(@Param("userId") Long userId);

    /**
     * Busca mensajes entre dos usuarios
     */
    @Query("SELECT dm FROM DirectMessage dm " +
            "WHERE dm.conversationId = :conversationId " +
            "AND dm.deleted = false " +
            "ORDER BY dm.createdAt DESC")
    List<DirectMessage> findByConversationId(
            @Param("conversationId") String conversationId,
            Pageable pageable
    );

    /**
     * Marca todos los mensajes como leídos
     */
    @Modifying // FIX: Necesario para que JPA ejecute la consulta UPDATE
    @Query("UPDATE DirectMessage dm SET dm.isRead = true, dm.readAt = CURRENT_TIMESTAMP " +
            "WHERE dm.conversationId = :conversationId " +
            "AND dm.receiver.id = :userId " +
            "AND dm.isRead = false")
    void markAllAsReadInConversation(
            @Param("conversationId") String conversationId,
            @Param("userId") Long userId
    );
}