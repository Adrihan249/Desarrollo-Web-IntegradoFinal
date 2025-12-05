package com.taskmanager.Repositorios;

import com.taskmanager.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    /**
     * Buscar una invitación por el email del invitado y el proyecto.
     * Útil para validar duplicados o invitaciones pendientes.
     */
    Optional<Invitation> findByInvitedEmailIgnoreCaseAndProjectIdAndStatus(
            String invitedEmail,
            Long projectId,
            Invitation.InvitationStatus status
    );

    /**
     * Buscar todas las invitaciones pendientes para un usuario por su email.
     * Retorna una LISTA porque puede tener múltiples invitaciones pendientes.
     */
    List<Invitation> findByInvitedEmailIgnoreCaseAndStatus(
            String invitedEmail,
            Invitation.InvitationStatus status
    );
    void deleteAllByProjectId(Long projectId);
    /**
     * 🔥 NUEVO: Buscar TODAS las invitaciones de un email (sin filtro de status)
     */
    List<Invitation> findByInvitedEmailIgnoreCase(String invitedEmail);

    /**
     * 🔥 NUEVO: Buscar invitaciones por proyecto
     */
    List<Invitation> findByProjectId(Long projectId);
}