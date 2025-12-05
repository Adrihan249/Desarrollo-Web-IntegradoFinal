package com.taskmanager.controller;

// ===================================
// CONTROLLERS DEL SPRINT 4
// Ubicación: com.taskmanager.controller
// ===================================

import com.taskmanager.dto.*;
import com.taskmanager.model.enums.*;
import com.taskmanager.security.CurrentUser;
import com.taskmanager.security.UserPrincipal;
import com.taskmanager.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

// ===================================
// PLAN CONTROLLER
// ===================================
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Slf4j
public class PlanController {

    private final PlanService planService;

    /**
     * Obtener todos los planes activos
     */
    @GetMapping
    public ResponseEntity<List<PlanDTO.Response>> getAllPlans() {
        log.info("GET /api/plans - Fetching all active plans");
        List<PlanDTO.Response> plans = planService.getAllActivePlans();
        return ResponseEntity.ok(plans);
    }

    /**
     * Obtener plan por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlanDTO.Response> getPlanById(@PathVariable Long id) {
        log.info("GET /api/plans/{} - Fetching plan", id);
        PlanDTO.Response plan = planService.getPlanById(id);
        return ResponseEntity.ok(plan);
    }

    /**
     * Obtener plan más popular
     */
    @GetMapping("/popular")
    public ResponseEntity<PlanDTO.Response> getMostPopularPlan() {
        log.info("GET /api/plans/popular - Fetching most popular plan");
        PlanDTO.Response plan = planService.getMostPopularPlan();
        return ResponseEntity.ok(plan);
    }

    /**
     * Crear plan (ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanDTO.Response> createPlan(
            @Valid @RequestBody PlanDTO.CreateRequest request
    ) {
        log.info("POST /api/plans - Creating new plan: {}", request.getName());
        PlanDTO.Response plan = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    /**
     * Actualizar plan (ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanDTO.Response> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody PlanDTO.CreateRequest request
    ) {
        log.info("PUT /api/plans/{} - Updating plan", id);
        PlanDTO.Response plan = planService.updatePlan(id, request);
        return ResponseEntity.ok(plan);
    }

    /**
     * Desactivar plan (ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivatePlan(@PathVariable Long id) {
        log.info("DELETE /api/plans/{} - Deactivating plan", id);
        planService.deactivatePlan(id);
        return ResponseEntity.noContent().build();
    }
}
