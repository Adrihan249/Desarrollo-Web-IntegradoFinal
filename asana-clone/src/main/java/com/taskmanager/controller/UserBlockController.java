
package com.taskmanager.controller;

import com.taskmanager.dto.DirectMessageDTO;
import com.taskmanager.dto.UserDTO;
import com.taskmanager.model.User;
import com.taskmanager.service.UserBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/blocks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class UserBlockController {

    private final UserBlockService blockService;

    /**
     * Bloquea a un usuario
     */
    @PostMapping
    public ResponseEntity<Void> blockUser(
            @Valid @RequestBody DirectMessageDTO.BlockRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/users/blocks");

        try {
            blockService.blockUser(
                    currentUser.getId(),
                    request.getBlockedUserId(),
                    request.getReason()
            );
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error blocking user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Desbloquea a un usuario
     */
    @DeleteMapping("/{blockedUserId}")
    public ResponseEntity<Void> unblockUser(
            @PathVariable Long blockedUserId,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/users/blocks/{}", blockedUserId);

        blockService.unblockUser(currentUser.getId(), blockedUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene lista de usuarios bloqueados
     */
    @GetMapping
    public ResponseEntity<List<UserDTO.Summary>> getBlockedUsers(
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/users/blocks");

        List<UserDTO.Summary> blockedUsers = blockService.getBlockedUsers(currentUser.getId());
        return ResponseEntity.ok(blockedUsers);
    }

    /**
     * Verifica si un usuario está bloqueado
     */
    @GetMapping("/check/{userId}")
    public ResponseEntity<Boolean> isBlocked(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/users/blocks/check/{}", userId);

        boolean blocked = blockService.isBlocked(currentUser.getId(), userId);
        return ResponseEntity.ok(blocked);
    }
}
