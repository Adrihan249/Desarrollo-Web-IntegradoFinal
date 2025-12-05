package com.taskmanager.controller;

import com.taskmanager.dto.InvitationDTO;
import com.taskmanager.model.User;
import com.taskmanager.service.ProjectService;
import com.taskmanager.service.InvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller para la gestión del flujo de invitaciones de proyectos.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class InvitationController {

    private final ProjectService projectService;
    private final InvitationService invitationService;

    // ======================================================
    // 📧 1. OBTENER INVITACIONES PENDIENTES DEL USUARIO
    // ======================================================

    /**
     * GET /api/invitations/pending
     * Obtiene la lista de invitaciones pendientes para el usuario logueado.
     */
    @GetMapping("/invitations/pending")
    public ResponseEntity<?> getPendingInvitations(
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No autenticado"));
        }

        log.info("GET /api/invitations/pending - User: {}", currentUser.getEmail());

        try {
            List<InvitationDTO.Response> invitations =
                    invitationService.getPendingInvitationsByEmail(currentUser.getEmail());

            return ResponseEntity.ok(invitations);

        } catch (Exception e) {
            log.error("Error fetching pending invitations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al obtener invitaciones"));
        }
    }

    // ======================================================
    // 🤝 2. RESPONDER A INVITACIÓN (ACEPTAR/RECHAZAR)
    // ======================================================

    /**
     * PUT /api/invitations/{invitationId}/respond
     * Permite al usuario responder a una invitación.
     * Body esperado: {"status": "ACCEPTED"} o {"status": "REJECTED"}
     */
    @PutMapping("/invitations/{invitationId}/respond")
    public ResponseEntity<?> respondToInvitation(
            @PathVariable Long invitationId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No autenticado"));
        }

        String status = request.get("status");

        if (status == null || (!status.equals("ACCEPTED") && !status.equals("REJECTED"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Estado inválido. Use ACCEPTED o REJECTED"));
        }

        log.info("PUT /api/invitations/{}/respond - User: {}, Status: {}",
                invitationId, currentUser.getEmail(), status);

        try {
            projectService.respondToInvitation(
                    invitationId,
                    status,
                    currentUser.getId()
            );

            String message = status.equals("ACCEPTED")
                    ? "Invitación aceptada. Ahora eres miembro del proyecto."
                    : "Invitación rechazada.";

            return ResponseEntity.ok(Map.of("message", message));

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            log.error("Error responding to invitation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al responder invitación"));
        }
    }

    // ======================================================
    // 🔔 3. OBTENER TODAS LAS INVITACIONES (HISTORIAL)
    // ======================================================

    /**
     * GET /api/invitations/all
     * Obtiene todas las invitaciones del usuario (pendientes, aceptadas, rechazadas)
     */
    @GetMapping("/invitations/all")
    public ResponseEntity<?> getAllInvitations(
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No autenticado"));
        }

        log.info("GET /api/invitations/all - User: {}", currentUser.getEmail());

        try {
            List<InvitationDTO.Response> invitations =
                    invitationService.getAllInvitationsByEmail(currentUser.getEmail());

            return ResponseEntity.ok(invitations);

        } catch (Exception e) {
            log.error("Error fetching all invitations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al obtener invitaciones"));
        }
    }
}