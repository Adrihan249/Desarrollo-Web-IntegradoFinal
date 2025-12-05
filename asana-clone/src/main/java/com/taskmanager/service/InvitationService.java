package com.taskmanager.service;

import com.taskmanager.Repositorios.InvitationRepository;
import com.taskmanager.dto.InvitationDTO;
import com.taskmanager.mapper.InvitationMapper;
import com.taskmanager.model.Invitation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de invitaciones a proyectos
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final InvitationMapper invitationMapper;

    /**
     * Obtiene todas las invitaciones pendientes por email del usuario
     */
    @Transactional(readOnly = true)
    public List<InvitationDTO.Response> getPendingInvitationsByEmail(String email) {
        log.info("Fetching pending invitations for email: {}", email);

        List<Invitation> invitations = invitationRepository
                .findByInvitedEmailIgnoreCaseAndStatus(
                        email,
                        Invitation.InvitationStatus.PENDING
                );

        return invitations.stream()
                .map(invitationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las invitaciones (pendientes, aceptadas, rechazadas) por email
     */
    @Transactional(readOnly = true)
    public List<InvitationDTO.Response> getAllInvitationsByEmail(String email) {
        log.info("Fetching all invitations for email: {}", email);

        List<Invitation> invitations = invitationRepository
                .findByInvitedEmailIgnoreCase(email);

        return invitations.stream()
                .map(invitationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}