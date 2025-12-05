package com.taskmanager.controller;

import com.taskmanager.dto.DirectMessageDTO;
import com.taskmanager.model.User;
import com.taskmanager.service.DirectMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/direct-messages")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class DirectMessageController {

    private final DirectMessageService messageService;

    /**
     * Envía un mensaje directo
     */
    @PostMapping
    public ResponseEntity<?> sendMessage(
            @Valid @RequestBody DirectMessageDTO.SendRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal User currentUser) {

        log.info("POST /api/direct-messages - User: {}, Receiver: {}",
                currentUser.getId(), request.getReceiverId());

        // Validar errores de binding
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.error("Validation errors: {}", errors);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errors);
        }

        try {
            DirectMessageDTO.Response message = messageService.sendMessage(
                    request,
                    currentUser.getId()
            );
            log.info("Message sent successfully: {}", message.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(message);

        } catch (IllegalArgumentException e) {
            log.warn("Illegal argument: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());

        } catch (org.springframework.security.access.AccessDeniedException e) {
            log.warn("Access denied: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Este usuario te ha bloqueado");

        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar mensaje: " + e.getMessage());
        }
    }

    /**
     * Obtiene todas las conversaciones del usuario
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<DirectMessageDTO.ConversationSummary>> getConversations(
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/direct-messages/conversations - User: {}", currentUser.getId());

        List<DirectMessageDTO.ConversationSummary> conversations =
                messageService.getConversations(currentUser.getId());

        return ResponseEntity.ok(conversations);
    }

    /**
     * Obtiene mensajes de una conversación específica
     */
    @GetMapping("/conversations/{otherUserId}")
    public ResponseEntity<List<DirectMessageDTO.Response>> getConversationMessages(
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/direct-messages/conversations/{} - User: {}",
                otherUserId, currentUser.getId());

        List<DirectMessageDTO.Response> messages = messageService.getConversationMessages(
                currentUser.getId(),
                otherUserId,
                page,
                size
        );

        return ResponseEntity.ok(messages);
    }

    /**
     * Edita un mensaje
     */
    @PutMapping("/{messageId}")
    public ResponseEntity<DirectMessageDTO.Response> updateMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody DirectMessageDTO.UpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/direct-messages/{} - User: {}", messageId, currentUser.getId());

        try {
            DirectMessageDTO.Response message = messageService.updateMessage(
                    messageId,
                    request,
                    currentUser.getId()
            );
            return ResponseEntity.ok(message);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Elimina un mensaje
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/direct-messages/{} - User: {}", messageId, currentUser.getId());

        try {
            messageService.deleteMessage(messageId, currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Agrega reacción
     */
    @PostMapping("/{messageId}/reactions")
    public ResponseEntity<DirectMessageDTO.Response> addReaction(
            @PathVariable Long messageId,
            @Valid @RequestBody DirectMessageDTO.ReactionRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/direct-messages/{}/reactions - User: {}",
                messageId, currentUser.getId());

        try {
            DirectMessageDTO.Response message = messageService.addReaction(
                    messageId,
                    request.getEmoji(),
                    currentUser.getId()
            );
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Remueve reacción
     */
    @DeleteMapping("/{messageId}/reactions/{emoji}")
    public ResponseEntity<DirectMessageDTO.Response> removeReaction(
            @PathVariable Long messageId,
            @PathVariable String emoji,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/direct-messages/{}/reactions/{} - User: {}",
                messageId, emoji, currentUser.getId());

        try {
            DirectMessageDTO.Response message = messageService.removeReaction(
                    messageId,
                    emoji,
                    currentUser.getId()
            );
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Marca mensaje como leído
     */
    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long messageId,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/direct-messages/{}/read - User: {}", messageId, currentUser.getId());

        messageService.markAsRead(messageId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Marca toda la conversación como leída
     */
    @PutMapping("/conversations/{otherUserId}/read")
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable Long otherUserId,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/direct-messages/conversations/{}/read - User: {}",
                otherUserId, currentUser.getId());

        messageService.markConversationAsRead(currentUser.getId(), otherUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cuenta mensajes no leídos
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnreadMessages(
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/direct-messages/unread/count - User: {}", currentUser.getId());

        long count = messageService.countUnreadMessages(currentUser.getId());
        return ResponseEntity.ok(count);
    }
}