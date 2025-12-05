package com.taskmanager.service;

// ===================================
// SERVICIOS DEL SPRINT 4 - PARTE 1
// Ubicación: com.taskmanager.service
// ===================================


import com.taskmanager.dto.*;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.mapper.*;
import com.taskmanager.model.*;
import com.taskmanager.model.enums.*;
import com.taskmanager.Repositorios.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// ===================================
// PLAN SERVICE
// ===================================
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanMapper planMapper;

    /**
     * Obtener todos los planes activos
     */
    @Transactional(readOnly = true)
    public List<PlanDTO.Response> getAllActivePlans() {
        log.info("Fetching all active plans");

        return planRepository.findByActiveTrue().stream()
                .map(plan -> {
                    PlanDTO.Response response = planMapper.toResponse(plan);
                    response.setActiveSubscriptions(
                            planRepository.countActiveSubscriptionsByPlan(plan.getId())
                    );
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtener plan por ID
     */
    @Transactional(readOnly = true)
    public PlanDTO.Response getPlanById(Long planId) {
        log.info("Fetching plan with ID: {}", planId);

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));

        PlanDTO.Response response = planMapper.toResponse(plan);
        response.setActiveSubscriptions(
                planRepository.countActiveSubscriptionsByPlan(planId)
        );

        return response;
    }

    /**
     * Obtener plan por nombre
     */
    @Transactional(readOnly = true)
    public PlanDTO.Response getPlanByName(String name) {
        log.info("Fetching plan with name: {}", name);

        Plan plan = planRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with name: " + name));

        return planMapper.toResponse(plan);
    }

    /**
     * Crear nuevo plan (ADMIN)
     */
    @Transactional
    public PlanDTO.Response createPlan(PlanDTO.CreateRequest request) {
        log.info("Creating new plan: {}", request.getName());

        // Verificar que no exista plan con el mismo nombre
        if (planRepository.existsByName(request.getName())) {
            throw new BadRequestException("Plan with name '" + request.getName() + "' already exists");
        }

        Plan plan = planMapper.toEntity(request);
        plan = planRepository.save(plan);

        log.info("Plan created successfully with ID: {}", plan.getId());
        return planMapper.toResponse(plan);
    }

    /**
     * Actualizar plan (ADMIN)
     */
    @Transactional
    public PlanDTO.Response updatePlan(Long planId, PlanDTO.CreateRequest request) {
        log.info("Updating plan with ID: {}", planId);

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));

        // Verificar nombre único (si cambió)
        if (!plan.getName().equals(request.getName()) &&
                planRepository.existsByName(request.getName())) {
            throw new BadRequestException("Plan with name '" + request.getName() + "' already exists");
        }

        planMapper.updateEntityFromRequest(request, plan);
        plan = planRepository.save(plan);

        log.info("Plan updated successfully");
        return planMapper.toResponse(plan);
    }

    /**
     * Desactivar plan (ADMIN)
     */
    @Transactional
    public void deactivatePlan(Long planId) {
        log.info("Deactivating plan with ID: {}", planId);

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));

        // Verificar que no haya suscripciones activas
        Long activeSubscriptions = planRepository.countActiveSubscriptionsByPlan(planId);
        if (activeSubscriptions > 0) {
            throw new BadRequestException(
                    "Cannot deactivate plan with " + activeSubscriptions + " active subscriptions"
            );
        }

        plan.setActive(false);
        planRepository.save(plan);

        log.info("Plan deactivated successfully");
    }

    /**
     * Obtener plan más popular
     */
    @Transactional(readOnly = true)
    public PlanDTO.Response getMostPopularPlan() {
        log.info("Fetching most popular plan");

        Plan plan = planRepository.findMostPopularPlan()
                .orElseThrow(() -> new ResourceNotFoundException("No plans found"));

        return planMapper.toResponse(plan);
    }
}
