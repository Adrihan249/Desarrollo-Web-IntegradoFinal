package com.taskmanager.Repositorios;

import com.taskmanager.model.*;
import com.taskmanager.model.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// ===================================
// PLAN REPOSITORY
// ===================================
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    // Planes activos
    List<Plan> findByActiveTrue();

    // Buscar por nombre
    Optional<Plan> findByName(String name);

    // Planes ordenados por precio
    List<Plan> findByActiveTrueOrderByPriceAsc();

    // Planes con precio en rango
    List<Plan> findByActiveTrueAndPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Verificar si existe plan con nombre
    boolean existsByName(String name);

    // Plan más popular
    @Query("SELECT p FROM Plan p WHERE p.id = " +
            "(SELECT s.plan.id FROM Subscription s " +
            "WHERE s.status = 'ACTIVE' " +
            "GROUP BY s.plan.id ORDER BY COUNT(s) DESC LIMIT 1)")
    Optional<Plan> findMostPopularPlan();

    // Contar suscripciones activas por plan
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.plan.id = :planId AND s.status = 'ACTIVE'")
    Long countActiveSubscriptionsByPlan(@Param("planId") Long planId);
}