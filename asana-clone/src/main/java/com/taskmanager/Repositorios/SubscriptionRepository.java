package com.taskmanager.Repositorios;

import com.taskmanager.model.*;
import com.taskmanager.model.enums.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// ===================================
// SUBSCRIPTION REPOSITORY
// ===================================
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // ===================================================================
    // MÉTODOS DE BÚSQUEDA ÚNICA (CRÍTICO)
    // ===================================================================

    /**
     * Busca la única suscripción ACTIVA o TRIAL de un usuario.
     * Carga ansiosa (EAGER) de 'user' y 'plan'.
     */
    @EntityGraph(attributePaths = {"user", "plan"})
    @Query("SELECT s FROM Subscription s " +
            "WHERE s.user.id = :userId AND (s.status = 'ACTIVE' OR s.status = 'TRIAL')")
    Optional<Subscription> findActiveOrTrialByUserId(@Param("userId") Long userId);

    /**
     * Carga ansiosa (EAGER) de 'user' y 'plan'.
     */
    @EntityGraph(attributePaths = {"user", "plan"})
    Optional<Subscription> findByUserId(Long userId);

    /**
     * Busca una suscripción por ID de usuario y estado específico.
     * Carga ansiosa (EAGER) de 'user' y 'plan' (Necesario para reactivación).
     */
    @EntityGraph(attributePaths = {"user", "plan"})
    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status); // 💡 MÉTODO AÑADIDO

    // Verificar si usuario tiene suscripción activa (solo ACTIVE)
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Subscription s " +
            "WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    boolean hasActiveSubscription(@Param("userId") Long userId);

    // ===================================================================
    // MÉTODOS DE BÚSQUEDA Y LISTADO
    // ===================================================================

    // Suscripciones por estado
    List<Subscription> findByStatus(SubscriptionStatus status);

    // Suscripciones activas
    List<Subscription> findByStatusOrderByCreatedAtDesc(SubscriptionStatus status);

    // Suscripciones que vencen pronto (ACTIVE o TRIAL)
    @Query("SELECT s FROM Subscription s WHERE " +
            "(s.status = 'ACTIVE' OR s.status = 'TRIAL') AND " +
            "s.endDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.endDate ASC")
    List<Subscription> findExpiringSubscriptions(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Suscripciones por plan
    List<Subscription> findByPlanIdOrderByCreatedAtDesc(Long planId);

    // Suscripciones con auto-renovación
    List<Subscription> findByAutoRenewTrueAndStatus(SubscriptionStatus status);

    // Suscripciones en trial que terminan pronto
    @Query("SELECT s FROM Subscription s WHERE " +
            "s.status = 'TRIAL' AND " +
            "s.trialEndDate BETWEEN :now AND :endDate " +
            "ORDER BY s.trialEndDate ASC")
    List<Subscription> findTrialsEndingSoon(
            @Param("now") LocalDateTime now,
            @Param("endDate") LocalDateTime endDate
    );

    // Suscripciones que requieren pago
    @Query("SELECT s FROM Subscription s WHERE " +
            "s.status = 'ACTIVE' AND " +
            "s.nextBillingDate <= :date AND " +
            "s.autoRenew = true")
    List<Subscription> findSubscriptionsRequiringPayment(@Param("date") LocalDateTime date);

    // ===================================================================
    // MÉTRICAS Y REPORTES
    // ===================================================================

    // Ingresos totales por período
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Subscription s WHERE " +
            "s.status = 'ACTIVE' AND " +
            "s.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Ingresos mensuales recurrentes (MRR)
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Subscription s WHERE " +
            "s.status = 'ACTIVE' AND s.billingPeriod = 'MONTHLY'")
    BigDecimal getMonthlyRecurringRevenue();

    // Ingresos anuales recurrentes (ARR)
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Subscription s WHERE " +
            "s.status = 'ACTIVE' AND s.billingPeriod = 'ANNUAL'")
    BigDecimal getAnnualRecurringRevenue();

    // Contar suscripciones por estado
    long countByStatus(SubscriptionStatus status);

    // Contar suscripciones por plan y estado (usado en ReportService)
    long countByPlanIdAndStatus(Long planId, SubscriptionStatus status);

    // Tasa de cancelación (Churn rate)
    @Query("SELECT COUNT(s) FROM Subscription s WHERE " +
            "s.status = 'CANCELLED' AND " +
            "s.cancelledAt BETWEEN :startDate AND :endDate")
    Long countCancelledSubscriptions(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Conversión de trial a pago (Suscripciones activas con trialEndDate no nulo en el período)
    @Query("SELECT COUNT(s) FROM Subscription s WHERE " +
            "s.status = 'ACTIVE' AND " +
            "s.trialEndDate IS NOT NULL AND " +
            "s.createdAt BETWEEN :startDate AND :endDate")
    Long countTrialConversions(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Contar nuevas suscripciones activas (usado en Growth Report)
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE' AND s.createdAt BETWEEN :start AND :end")
    Long countNewActiveSubscriptions(@Param("start") LocalDateTime startDate, @Param("end") LocalDateTime endDate);

    // Contar reactivaciones (usado en Growth Report - asume que 'updatedAt' refleja la reactivación)
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE' AND s.cancelledAt IS NOT NULL AND s.updatedAt BETWEEN :start AND :end")
    Long countReactivatedSubscriptions(@Param("start") LocalDateTime startDate, @Param("end") LocalDateTime endDate);

    // Contar trials iniciados en el período (usado en Conversion Metrics)
    long countByStatusAndCreatedAtBetween(SubscriptionStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // Ingresos por Plan (usado en ReportService)
    @Query("SELECT p.name, COALESCE(SUM(s.amount), 0) FROM Subscription s JOIN s.plan p " +
            "WHERE s.status = 'ACTIVE' GROUP BY p.name")
    List<Object[]> getRevenueByPlanAggregated();

    // ===================================================================
    // REQUERIMIENTOS DE USO (CONTADOR DE LÍMITES)
    // ===================================================================

    // Contar proyectos del usuario (para límites del plan)
    @Query("SELECT COUNT(p) FROM Project p WHERE " +
            "p.createdBy.id = :userId AND p.archived = false")
    Integer countUserProjects(@Param("userId") Long userId);

    /**
     * Cuenta el número total de miembros (usuarios) asociados a proyectos
     * creados por un usuario específico (dueño de la suscripción).
     * Navega a través de la colección 'members' de la entidad Project.
     */
    @Query("SELECT COUNT(DISTINCT m.id) FROM Project p JOIN p.members m " +
            "WHERE p.createdBy.id = :userId")
    Integer countMembersByUserId(@Param("userId") Long userId);
}