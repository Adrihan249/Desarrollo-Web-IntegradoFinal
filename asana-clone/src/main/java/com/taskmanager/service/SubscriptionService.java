package com.taskmanager.service;

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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final NotificationService notificationService;

    // ===================================================================
    // LECTURA Y ESTADO (CORREGIDO PARA BUSCAR SOLO LA ACTIVA)
    // ===================================================================

    /**
     * Devuelve la suscripción ACTIVA o TRIAL. Si no hay, devuelve error 404.
     */
    @Transactional(readOnly = true)
    public SubscriptionDTO.Response getCurrentSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findActiveOrTrialByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No subscription found for user ID: " + userId
                ));

        return subscriptionMapper.toResponse(subscription);
    }

    /**
     * Verifica la existencia de una suscripción ACTIVA o TRIAL.
     */
    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveOrTrialByUserId(userId).isPresent();
    }

    // ===================================================================
    // VERIFICACIÓN DE LÍMITES (CORREGIDO PARA USAR SOLO SUSCRIPCIÓN ACTIVA)
    // ===================================================================

    /**
     * Obtiene la suscripción activa o el plan FREE si no hay ninguna.
     */
    private Subscription getActiveSubscriptionOrDefaultFree(Long userId) {
        Optional<Subscription> activeSub = subscriptionRepository.findActiveOrTrialByUserId(userId);

        if (activeSub.isPresent()) {
            return activeSub.get();
        }

        Plan freePlan = planRepository.findByName("Free")
                .orElse(null);

        if (freePlan == null) {
            log.warn("Plan 'Free' not found in database. Limits checks are disabled.");
            return null;
        }

        // Creamos una entidad temporal (no persistida) para verificar límites
        Subscription freeSubscription = new Subscription();
        freeSubscription.setPlan(freePlan);
        freeSubscription.setCurrentProjects(projectRepository.countByCreatedById(userId));
        // Recalcular miembros y storage para la suscripción gratuita temporal
        freeSubscription.setCurrentMembers(subscriptionRepository.countMembersByUserId(userId));
        // NOTA: No podemos calcular currentStorageUsed sin el repositorio de archivos,
        // asumimos 0 para el default si no hay lógica de archivos aquí.
        freeSubscription.setCurrentStorageUsed(0L);
        return freeSubscription;
    }


    /**
     * Verifica si el usuario puede crear un proyecto más
     */
    @Transactional(readOnly = true)
    public boolean canCreateProject(Long userId) {
        // Usar el método que ya creaste para obtener la suscripción activa o el plan GRATUITO
        Subscription subscription = getActiveSubscriptionOrDefaultFree(userId);

        // Si getActiveSubscriptionOrDefaultFree devuelve null (por ejemplo, si no encuentra el plan 'Free'),
        // asumimos que no hay límites, aunque es mejor lanzar un error.
        if (subscription == null || subscription.getPlan() == null) {
            log.warn("Cannot check project limits for user {} due to missing Plan configuration.", userId);
            return true; // Permitir la creación por seguridad si la verificación falla
        }

        // Contar SOLO proyectos no archivados
        int activeProjects = projectRepository.countByCreatedByIdAndArchivedFalse(userId);

        // ✅ CORRECCIÓN: Acceder al límite a través de subscription.getPlan()
        int maxProjects = subscription.getPlan().getMaxProjects();

        log.info("User {} has {} active projects, limit is {}",
                userId, activeProjects, maxProjects);

        // Si el límite es -1 (Ilimitado), siempre devuelve true.
        if (maxProjects == -1) {
            return true;
        }

        return activeProjects < maxProjects;
    }
    /**
     * Obtiene el límite máximo de miembros permitido por la suscripción activa del usuario.
     * @param userId ID del usuario (creador del proyecto).
     * @return El número máximo de miembros permitido.
     */
    @Transactional(readOnly = true)
    public int getMemberLimit(Long userId) {
        // ⚠️ IMPLEMENTACIÓN DE EJEMPLO/MOCK: DEBES ADAPTAR ESTO A TU LÓGICA REAL DE SUBSCRIPCIONES.

        // 1. Obtener la suscripción activa del usuario (asumiendo un método como findActiveSubscriptionByUserId)
        // Optional<Subscription> subscriptionOpt = subscriptionRepository.findActiveSubscriptionByUserId(userId);

        // if (subscriptionOpt.isPresent()) {
        //     Subscription subscription = subscriptionOpt.get();

        //     // 2. Devolver el límite basado en el plan (ej. Basic, Pro, Enterprise)
        //     return subscription.getPlan().getMaxMembers();
        // }

        // MOCK RÁPIDO para permitir la compilación y prueba inicial:
        // Si el usuario no tiene suscripción activa o es el plan "Gratis" por defecto

        // Ejemplo de límite:
        // Plan Básico/Gratis: 5 miembros
        // Plan Pro: 50 miembros

        // Aquí puedes implementar la lógica para buscar el plan:
        // User user = userRepository.findById(userId).orElse(null);
        // if (user != null && user.getPlan().equals("PRO")) {
        //     return 50;
        // }

        // Por defecto, si no se encuentra el plan o es el plan gratuito:
        return 5; // Límite de miembros por defecto para el plan gratuito
    }
    /**
     * Verifica si el usuario puede agregar un miembro más
     */
    @Transactional(readOnly = true)
    public boolean canAddMember(Long userId) {
        Subscription subscription = getActiveSubscriptionOrDefaultFree(userId);

        if (subscription == null) return true;

        Plan plan = subscription.getPlan();
        // Recalculamos miembros para ser precisos, ya que la suscripción gratuita es temporal
        int currentMembers = subscriptionRepository.countMembersByUserId(userId);

        return plan.getMaxMembers() == -1 ||
                currentMembers < plan.getMaxMembers();
    }

    /**
     * Verifica si el usuario puede subir más archivos
     */
    @Transactional(readOnly = true)
    public boolean canUploadFile(Long userId, long fileSize) {
        Subscription subscription = getActiveSubscriptionOrDefaultFree(userId);

        if (subscription == null) return true;

        Plan plan = subscription.getPlan();
        // Usar el valor recalculado en getActiveSubscriptionOrDefaultFree si no está persistido
        long currentStorage = subscription.getCurrentStorageUsed();
        long maxStorageBytes = plan.getMaxStorage() * 1024L * 1024L; // MB to bytes

        return plan.getMaxStorage() == -1 ||
                (currentStorage + fileSize) <= maxStorageBytes;
    }

    // ===================================================================
    // CREACIÓN Y CAMBIO DE PLAN
    // ===================================================================

    /**
     * CREACIÓN DE SUSCRIPCIÓN
     */
    @Transactional
    public SubscriptionDTO.Response createSubscription(
            Long userId,
            SubscriptionDTO.CreateRequest request
    ) {
        log.info("Creating subscription for user ID: {} with plan ID: {}", userId, request.getPlanId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + request.getPlanId()));

        if (!plan.getActive()) {
            throw new BadRequestException("Plan is not active");
        }

        // 1. ANULAR O CANCELAR CUALQUIER SUSCRIPCIÓN ACTIVA/TRIAL ANTERIOR
        subscriptionRepository.findActiveOrTrialByUserId(userId)
                .ifPresent(this::cancelCurrentSubscriptionImmediately);


        // 2. CREAR LA NUEVA SUSCRIPCIÓN
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime trialEnd = now.plusDays(plan.getTrialDays());
        LocalDateTime endDate = calculateEndDate(trialEnd, request.getBillingPeriod());
        BigDecimal amount = calculateAmount(plan, request.getBillingPeriod());

        int currentProjects = projectRepository.countByCreatedById(userId);
        int currentMembers = subscriptionRepository.countMembersByUserId(userId); // Recalcular miembros

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(plan.getTrialDays() > 0 ? SubscriptionStatus.TRIAL : SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(endDate)
                .trialEndDate(plan.getTrialDays() > 0 ? trialEnd : null)
                .billingPeriod(request.getBillingPeriod())
                .amount(amount)
                .currency("USD")
                .nextBillingDate(plan.getTrialDays() > 0 ? trialEnd : calculateNextBillingDate(now, request.getBillingPeriod())) // Usar auxiliar
                .autoRenew(true)
                .paymentMethod(request.getPaymentMethod())
                .currentProjects(currentProjects)
                .currentMembers(currentMembers)
                .currentStorageUsed(0L)
                .totalPaid(BigDecimal.ZERO)
                .renewalCount(0)
                .build();

        subscription = subscriptionRepository.save(subscription);

        if (subscription.getStatus() == SubscriptionStatus.TRIAL) {
            notificationService.createTrialEndingNotification(user, trialEnd);
        }

        log.info("Subscription created successfully with ID: {}", subscription.getId());
        return subscriptionMapper.toResponse(subscription);
    }

    /**
     * CAMBIO DE PLAN
     */
    @Transactional
    public SubscriptionDTO.Response changePlan(
            Long userId,
            SubscriptionDTO.ChangePlanRequest request
    ) {
        log.info("Changing plan for user ID: {} to plan ID: {}", userId, request.getNewPlanId());

        Subscription subscription = subscriptionRepository.findActiveOrTrialByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found for user"));

        Plan oldPlan = subscription.getPlan();
        Plan newPlan = planRepository.findById(request.getNewPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        if (!newPlan.getActive()) {
            throw new BadRequestException("Plan is not active");
        }

        // 1. Validar límites ANTES de aplicar el cambio
        validatePlanLimits(subscription, newPlan);

        // 2. Si el cambio es inmediato, finalizamos la suscripción anterior AHORA y creamos la nueva
        if (Boolean.TRUE.equals(request.getImmediate())) {

            cancelCurrentSubscriptionImmediately(subscription);

            SubscriptionDTO.CreateRequest createRequest = new SubscriptionDTO.CreateRequest();
            createRequest.setPlanId(newPlan.getId());
            createRequest.setBillingPeriod(request.getBillingPeriod());
            createRequest.setPaymentMethod(request.getPaymentMethod());

            return this.createSubscription(userId, createRequest);

        } else {
            // 3. Si el cambio es en la renovación (opción más sencilla)
            subscription.setPlan(newPlan);
            subscription.setBillingPeriod(request.getBillingPeriod());
            subscription.setAmount(calculateAmount(newPlan, request.getBillingPeriod()));

            subscription = subscriptionRepository.save(subscription);

            notificationService.createPlanChangedNotification(
                    subscription.getUser(),
                    oldPlan.getName(),
                    newPlan.getName()
            );

            log.info("Plan change scheduled for next billing cycle");
            return subscriptionMapper.toResponse(subscription);
        }
    }

    // ===================================================================
    // CANCELACIÓN Y REACTIVACIÓN
    // ===================================================================

    @Transactional
    public void cancelSubscription(Long userId, SubscriptionDTO.CancelRequest request) {
        log.info("Cancelling subscription for user ID: {}", userId);

        Subscription subscription = subscriptionRepository.findActiveOrTrialByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found for user"));

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new BadRequestException("Subscription is already cancelled");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setAutoRenew(false);

        if (Boolean.TRUE.equals(request.getImmediate())) {
            subscription.setEndDate(LocalDateTime.now());
        }

        subscriptionRepository.save(subscription);
        log.info("Subscription cancelled successfully");
    }

    @Transactional
    public SubscriptionDTO.Response reactivateSubscription(Long userId) {
        log.info("Reactivating subscription for user ID: {}", userId);

        // 💡 CORRECCIÓN: Este método requiere findByUserIdAndStatus(userId, SubscriptionStatus.CANCELLED)
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.CANCELLED)
                .orElseThrow(() -> new ResourceNotFoundException("No cancelled subscription found for user"));

        // 1. Finalizar cualquier otra suscripción activa por si acaso
        subscriptionRepository.findActiveOrTrialByUserId(userId)
                .ifPresent(this::cancelCurrentSubscriptionImmediately);

        // 2. Reactivar la suscripción
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setCancelledAt(null);
        subscription.setAutoRenew(true);

        LocalDateTime now = LocalDateTime.now();
        // Calcula la nueva fecha de finalización (1 mes o 1 año desde ahora)
        subscription.setEndDate(calculateEndDate(now, subscription.getBillingPeriod()));
        subscription.setNextBillingDate(calculateNextBillingDate(now, subscription.getBillingPeriod()));

        subscription = subscriptionRepository.save(subscription);

        log.info("Subscription reactivated successfully");
        return subscriptionMapper.toResponse(subscription);
    }

    // ===================================================================
    // USO Y SINCRONIZACIÓN
    // ===================================================================

    @Transactional(readOnly = true)
    public SubscriptionDTO.UsageSummary getUsageSummary(Long userId) {
        log.info("Fetching usage summary for user ID: {}", userId);

        Subscription subscription = subscriptionRepository.findActiveOrTrialByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found for user"));

        Plan plan = subscription.getPlan();

        // Recalcular conteo de proyectos (Sincronización)
        Integer currentProjects = projectRepository.countByCreatedById(userId);
        subscription.setCurrentProjects(currentProjects);

        // Recalcular conteo de miembros (Sincronización)
        Integer currentMembers = subscriptionRepository.countMembersByUserId(userId);
        subscription.setCurrentMembers(currentMembers);

        // NOTE: Asumimos que currentStorageUsed es actualizado por un servicio de archivos
        // Si no, también debe recalcularse aquí.
        // long currentStorage = storageService.getUsedStorage(userId);
        // subscription.setCurrentStorageUsed(currentStorage);

        subscriptionRepository.save(subscription); // Guardar los contadores actualizados

        return SubscriptionDTO.UsageSummary.builder()
                .projects(createUsageMetric(currentProjects, plan.getMaxProjects()))
                .members(createUsageMetric(subscription.getCurrentMembers(), plan.getMaxMembers()))
                .storage(createUsageMetric(
                        (int)(subscription.getCurrentStorageUsed() / (1024 * 1024)),
                        plan.getMaxStorage()
                ))
                .build();
    }

    /**
     * Sincroniza el contador de proyectos
     */
    @Transactional
    public void updateProjectCount(Long userId, int change) {
        subscriptionRepository.findActiveOrTrialByUserId(userId)
                .ifPresent(subscription -> {
                    int newCount = Math.max(0, subscription.getCurrentProjects() + change);
                    subscription.setCurrentProjects(newCount);
                    subscriptionRepository.save(subscription);

                    log.debug("Project count updated for user {}: {} -> {}",
                            userId, subscription.getCurrentProjects() - change, newCount);
                });
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Finaliza la suscripción inmediatamente
     */
    private void cancelCurrentSubscriptionImmediately(Subscription subscription) {
        log.warn("Terminating subscription ID: {} immediately due to new subscription/plan change.", subscription.getId());
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now());
        subscription.setAutoRenew(false);
        subscriptionRepository.save(subscription);
    }

    private LocalDateTime calculateEndDate(LocalDateTime startDate, BillingPeriod period) {
        return period == BillingPeriod.ANNUAL
                ? startDate.plusYears(1)
                : startDate.plusMonths(1);
    }

    private LocalDateTime calculateNextBillingDate(LocalDateTime startDate, BillingPeriod period) {
        // En un escenario real, esto debería considerar el día de pago del usuario (siempre el día 5, etc.)
        return period == BillingPeriod.ANNUAL
                ? startDate.plusYears(1)
                : startDate.plusMonths(1);
    }

    private BigDecimal calculateAmount(Plan plan, BillingPeriod period) {
        return period == BillingPeriod.ANNUAL && plan.getAnnualPrice() != null
                ? plan.getAnnualPrice()
                : plan.getPrice();
    }

    private void validatePlanLimits(Subscription subscription, Plan newPlan) {
        if (newPlan.getMaxProjects() != -1 &&
                subscription.getCurrentProjects() > newPlan.getMaxProjects()) {
            throw new BadRequestException(
                    String.format("Current project count (%d) exceeds new plan limit (%d)",
                            subscription.getCurrentProjects(), newPlan.getMaxProjects())
            );
        }

        if (newPlan.getMaxMembers() != -1 &&
                subscription.getCurrentMembers() > newPlan.getMaxMembers()) {
            throw new BadRequestException(
                    String.format("Current member count (%d) exceeds new plan limit (%d)",
                            subscription.getCurrentMembers(), newPlan.getMaxMembers())
            );
        }

        long currentStorageMB = subscription.getCurrentStorageUsed() / (1024 * 1024);
        if (newPlan.getMaxStorage() != -1 &&
                currentStorageMB > newPlan.getMaxStorage()) {
            throw new BadRequestException(
                    String.format("Current storage usage (%d MB) exceeds new plan limit (%d MB)",
                            currentStorageMB, newPlan.getMaxStorage())
            );
        }
    }

    private SubscriptionDTO.UsageMetric createUsageMetric(Integer used, Integer limit) {
        boolean isUnlimited = limit == -1;
        int percentage = isUnlimited ? 0 : (used * 100 / Math.max(limit, 1));

        return SubscriptionDTO.UsageMetric.builder()
                .used(used)
                .limit(isUnlimited ? null : limit)
                .percentage(percentage)
                .isNearLimit(!isUnlimited && percentage >= 80)
                .isOverLimit(!isUnlimited && used > limit)
                .build();
    }

    // Métodos omitidos por brevedad (updateMemberCount, updateStorageUsage, etc.)
    // Asegúrate de implementarlos usando la misma lógica de findActiveOrTrialByUserId.
}