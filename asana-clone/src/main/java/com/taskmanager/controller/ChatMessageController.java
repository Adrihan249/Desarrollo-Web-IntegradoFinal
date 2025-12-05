package com.taskmanager.controller;

import com.taskmanager.dto.*;
import com.taskmanager.model.User;
import com.taskmanager.service.ChatMessageService;
import com.taskmanager.service.ActivityLogService;
import com.taskmanager.service.FilterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST del Chat del Proyecto
 *
 * CUMPLE REQUERIMIENTO N°14: Chat del proyecto
 */
@RestController
@RequestMapping("/api/projects/{projectId}/chat/messages")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    /**
     * N°14: Envía un mensaje
     * POST /api/projects/{projectId}/chat/messages
     */
    @PostMapping
    public ResponseEntity<ChatMessageDTO.Response> sendMessage(
            @PathVariable Long projectId,
            @Valid @RequestBody ChatMessageDTO.SendRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/projects/{}/chat/messages", projectId);

        try {
            ChatMessageDTO.Response message = chatMessageService.sendMessage(
                    projectId, request, currentUser.getId()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(message);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    /**
     * N°14: Obtiene mensajes del proyecto
     * GET /api/projects/{projectId}/chat/messages?page=0&size=50
     */
    @GetMapping
    public ResponseEntity<List<ChatMessageDTO.Response>> getMessages(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/chat/messages?page={}&size={}", projectId, page, size);

        try {
            List<ChatMessageDTO.Response> messages = chatMessageService.getProjectMessages(
                    projectId, currentUser.getId(), page, size
            );

            return ResponseEntity.ok(messages);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
    }

    /**
     * N°14: Obtiene mensajes recientes (últimas 24h)
     * GET /api/projects/{projectId}/chat/messages/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ChatMessageDTO.Response>> getRecentMessages(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/chat/messages/recent", projectId);

        try {
            List<ChatMessageDTO.Response> messages = chatMessageService
                    .getRecentMessages(projectId, currentUser.getId());

            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
    }

    /**
     * N°14: Obtiene respuestas a un mensaje
     * GET /api/projects/{projectId}/chat/messages/{id}/replies
     */
    @GetMapping("/{id}/replies")
    public ResponseEntity<List<ChatMessageDTO.Response>> getReplies(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/chat/messages/{}/replies", projectId, id);

        try {
            List<ChatMessageDTO.Response> replies = chatMessageService
                    .getMessageReplies(id, currentUser.getId());

            return ResponseEntity.ok(replies);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°14: Edita un mensaje
     * PUT /api/projects/{projectId}/chat/messages/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ChatMessageDTO.Response> updateMessage(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody ChatMessageDTO.UpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/projects/{}/chat/messages/{}", projectId, id);

        try {
            ChatMessageDTO.Response message = chatMessageService.updateMessage(
                    id, request, currentUser.getId()
            );

            return ResponseEntity.ok(message);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°14: Elimina un mensaje
     * DELETE /api/projects/{projectId}/chat/messages/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/projects/{}/chat/messages/{}", projectId, id);

        try {
            chatMessageService.deleteMessage(id, currentUser.getId());
            return ResponseEntity.noContent().build();

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°14: Agrega reacción a un mensaje
     * POST /api/projects/{projectId}/chat/messages/{id}/reactions
     */
    @PostMapping("/{id}/reactions")
    public ResponseEntity<ChatMessageDTO.Response> addReaction(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody ChatMessageDTO.ReactionRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/projects/{}/chat/messages/{}/reactions - {}",
                projectId, id, request.getEmoji());

        try {
            ChatMessageDTO.Response message = chatMessageService.addReaction(
                    id, request.getEmoji(), currentUser.getId()
            );

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°14: Remueve reacción
     * DELETE /api/projects/{projectId}/chat/messages/{id}/reactions/{emoji}
     */
    @DeleteMapping("/{id}/reactions/{emoji}")
    public ResponseEntity<ChatMessageDTO.Response> removeReaction(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @PathVariable String emoji,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/projects/{}/chat/messages/{}/reactions/{}", projectId, id, emoji);

        try {
            ChatMessageDTO.Response message = chatMessageService.removeReaction(
                    id, emoji, currentUser.getId()
            );

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°14: Fija un mensaje
     * PUT /api/projects/{projectId}/chat/messages/{id}/pin
     */
    @PutMapping("/{id}/pin")
    public ResponseEntity<ChatMessageDTO.Response> pinMessage(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/projects/{}/chat/messages/{}/pin", projectId, id);

        try {
            ChatMessageDTO.Response message = chatMessageService.pinMessage(
                    id, currentUser.getId()
            );

            return ResponseEntity.ok(message);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°14: Desfija un mensaje
     * DELETE /api/projects/{projectId}/chat/messages/{id}/pin
     */
    @DeleteMapping("/{id}/pin")
    public ResponseEntity<ChatMessageDTO.Response> unpinMessage(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/projects/{}/chat/messages/{}/pin", projectId, id);

        try {
            ChatMessageDTO.Response message = chatMessageService.unpinMessage(
                    id, currentUser.getId()
            );

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
    }

    /**
     * N°14: Obtiene mensajes fijados
     * GET /api/projects/{projectId}/chat/messages/pinned
     */
    @GetMapping("/pinned")
    public ResponseEntity<List<ChatMessageDTO.Response>> getPinnedMessages(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/chat/messages/pinned", projectId);

        try {
            List<ChatMessageDTO.Response> messages = chatMessageService
                    .getPinnedMessages(projectId, currentUser.getId());

            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
    }

    /**
     * N°14: Marca mensaje como leído
     * PUT /api/projects/{projectId}/chat/messages/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/projects/{}/chat/messages/{}/read", projectId, id);

        chatMessageService.markAsRead(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * N°14: Obtiene mensajes no leídos
     * GET /api/projects/{projectId}/chat/messages/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<List<ChatMessageDTO.Response>> getUnreadMessages(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/chat/messages/unread", projectId);

        try {
            List<ChatMessageDTO.Response> messages = chatMessageService
                    .getUnreadMessages(projectId, currentUser.getId());

            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
    }

    /**
     * N°14: Cuenta mensajes no leídos
     * GET /api/projects/{projectId}/chat/messages/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnreadMessages(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/chat/messages/unread/count", projectId);

        try {
            long count = chatMessageService.countUnreadMessages(projectId, currentUser.getId());
            return ResponseEntity.ok(count);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
    }

    /**
     * N°13: Busca mensajes
     * GET /api/projects/{projectId}/chat/messages/search?keyword=bug
     */
    @GetMapping("/search")
    public ResponseEntity<List<ChatMessageDTO.Response>> searchMessages(
            @PathVariable Long projectId,
            @RequestParam String keyword,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/chat/messages/search?keyword={}", projectId, keyword);

        try {
            List<ChatMessageDTO.Response> messages = chatMessageService
                    .searchMessages(projectId, keyword, currentUser.getId());

            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
    }
}
