package com.taskmanager.controller;

import com.taskmanager.dto.SubscriptionDTO;
import com.taskmanager.model.User;
import com.taskmanager.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


/**
 * Controlador de Suscripciones
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Obtener suscripción actual del usuario autenticado
     */
    @GetMapping("/current")
    public ResponseEntity<SubscriptionDTO.Response> getCurrentSubscription(
            @AuthenticationPrincipal User user) {

        log.info("GET /subscriptions/current - User ID: {}", user.getId());

        SubscriptionDTO.Response response = subscriptionService.getCurrentSubscription(user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Crear nueva suscripción
     * CORREGIDO: Usar @AuthenticationPrincipal para obtener el usuario
     */
    @PostMapping
    public ResponseEntity<SubscriptionDTO.Response> createSubscription(
            @Valid @RequestBody SubscriptionDTO.CreateRequest request,
            @AuthenticationPrincipal User user) {

        // Verificación de seguridad adicional
        if (user == null) {
            log.error("POST /subscriptions failed: User not authenticated.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        log.info("POST /subscriptions - User ID: {}, Plan ID: {}",
                user.getId(), request.getPlanId());

        SubscriptionDTO.Response response = subscriptionService.createSubscription(
                user.getId(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cambiar plan de suscripción
     */
    @PutMapping("/change-plan")
    public ResponseEntity<SubscriptionDTO.Response> changePlan(
            @Valid @RequestBody SubscriptionDTO.ChangePlanRequest request,
            @AuthenticationPrincipal User user) {

        log.info("PUT /subscriptions/change-plan - User ID: {}, New Plan ID: {}",
                user.getId(), request.getNewPlanId());

        SubscriptionDTO.Response response = subscriptionService.changePlan(
                user.getId(),
                request
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Cancelar suscripción
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelSubscription(
            @Valid @RequestBody SubscriptionDTO.CancelRequest request,
            @AuthenticationPrincipal User user) {

        log.info("POST /subscriptions/cancel - User ID: {}", user.getId());

        subscriptionService.cancelSubscription(user.getId(), request);

        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivar suscripción cancelada
     */
    @PostMapping("/reactivate")
    public ResponseEntity<SubscriptionDTO.Response> reactivateSubscription(
            @AuthenticationPrincipal User user) {

        log.info("POST /subscriptions/reactivate - User ID: {}", user.getId());

        SubscriptionDTO.Response response = subscriptionService.reactivateSubscription(user.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener resumen de uso
     */
    @GetMapping("/usage")
    public ResponseEntity<SubscriptionDTO.UsageSummary> getUsageSummary(
            @AuthenticationPrincipal User user) {

        log.info("GET /subscriptions/usage - User ID: {}", user.getId());

        SubscriptionDTO.UsageSummary summary = subscriptionService.getUsageSummary(user.getId());

        return ResponseEntity.ok(summary);
    }

    /**
     * Verificar si tiene suscripción activa
     */
    @GetMapping("/active")
    public ResponseEntity<Boolean> hasActiveSubscription(
            @AuthenticationPrincipal User user) {

        log.info("GET /subscriptions/active - User ID: {}", user.getId());

        boolean hasActive = subscriptionService.hasActiveSubscription(user.getId());

        return ResponseEntity.ok(hasActive);
    }

    /**
     * MÉTODO ALTERNATIVO: Si @AuthenticationPrincipal no funciona
     * Extrae el usuario manualmente del SecurityContext
     */
    private Long getUserIdFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authentication found in SecurityContext");
            throw new IllegalStateException("Authentication required to create a subscription.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User user = (User) principal;
            log.debug("User extracted from SecurityContext: ID={}, Email={}",
                    user.getId(), user.getEmail());
            return user.getId();
        }

        log.error("Principal is not an instance of User: {}", principal.getClass().getName());
        throw new IllegalStateException("Invalid authentication principal type.");
    }

    /**
     * VERSIÓN ALTERNATIVA del método createSubscription usando extracción manual
     * Usar este si @AuthenticationPrincipal no funciona
     */
    @PostMapping("/v2")
    public ResponseEntity<SubscriptionDTO.Response> createSubscriptionV2(
            @Valid @RequestBody SubscriptionDTO.CreateRequest request) {

        Long userId = getUserIdFromContext();

        log.info("POST /subscriptions/v2 - User ID: {}, Plan ID: {}",
                userId, request.getPlanId());

        SubscriptionDTO.Response response = subscriptionService.createSubscription(
                userId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}