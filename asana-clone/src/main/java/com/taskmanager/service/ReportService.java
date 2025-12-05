package com.taskmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import com.taskmanager.model.enums.*;
import com.taskmanager.Repositorios.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
// ===================================
// REPORT SERVICE
// ===================================
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    /**
     * Dashboard de métricas de suscripción
     */
    @Transactional(readOnly = true)
    public SubscriptionReportDTO.Dashboard getDashboard() {
        log.info("Generating subscription dashboard");

        return SubscriptionReportDTO.Dashboard.builder()
                .summary(getMetricsSummary())
                .revenue(getRevenueMetrics())
                .conversions(getConversionMetrics())
                .subscriptionsByPlan(getSubscriptionsByPlan())
                .revenueByPlan(getRevenueByPlan())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Resumen de métricas
     */
    private SubscriptionReportDTO.MetricsSummary getMetricsSummary() {
        long total = subscriptionRepository.count();
        long active = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        long trial = subscriptionRepository.countByStatus(SubscriptionStatus.TRIAL);
        long cancelled = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);
        long suspended = subscriptionRepository.countByStatus(SubscriptionStatus.SUSPENDED);

        // Calcular tasa de crecimiento (último mes)
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        LocalDateTime now = LocalDateTime.now();

        return SubscriptionReportDTO.MetricsSummary.builder()
                .totalSubscriptions((int) total)
                .activeSubscriptions((int) active)
                .trialSubscriptions((int) trial)
                .cancelledSubscriptions((int) cancelled)
                .suspendedSubscriptions((int) suspended)
                .growthRate(calculateGrowthRate(monthAgo, now))
                .build();
    }

    /**
     * Métricas de ingresos
     */
    private SubscriptionReportDTO.RevenueMetrics getRevenueMetrics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime yearStart = now.withDayOfYear(1).withHour(0).withMinute(0);

        BigDecimal monthlyRevenue = subscriptionRepository.getTotalRevenue(monthStart, now);
        BigDecimal annualRevenue = subscriptionRepository.getTotalRevenue(yearStart, now);
        BigDecimal mrr = subscriptionRepository.getMonthlyRecurringRevenue();
        BigDecimal arr = subscriptionRepository.getAnnualRecurringRevenue();

        long activeSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        BigDecimal arpu = activeSubscriptions > 0
                ? mrr.divide(BigDecimal.valueOf(activeSubscriptions), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        return SubscriptionReportDTO.RevenueMetrics.builder()
                .monthlyRevenue(monthlyRevenue)
                .annualRevenue(annualRevenue)
                .totalRevenue(annualRevenue)
                .averageRevenuePerUser(arpu)
                .monthlyRecurringRevenue(mrr)
                .annualRecurringRevenue(arr)
                .projectedMonthlyRevenue(mrr.multiply(BigDecimal.valueOf(12)))
                .build();
    }

    /**
     * Métricas de conversión
     */
    private SubscriptionReportDTO.ConversionMetrics getConversionMetrics() {
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        LocalDateTime now = LocalDateTime.now();

        Long trialStarted = subscriptionRepository.countByStatus(SubscriptionStatus.TRIAL);
        Long trialConverted = subscriptionRepository.countTrialConversions(monthAgo, now);
        Long cancelled = subscriptionRepository.countCancelledSubscriptions(monthAgo, now);
        Long active = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);

        double conversionRate = trialStarted > 0
                ? (trialConverted.doubleValue() / trialStarted.doubleValue()) * 100
                : 0.0;

        double cancellationRate = active > 0
                ? (cancelled.doubleValue() / active.doubleValue()) * 100
                : 0.0;

        double retentionRate = 100.0 - cancellationRate;

        return SubscriptionReportDTO.ConversionMetrics.builder()
                .trialStarted(trialStarted.intValue())
                .trialConverted(trialConverted.intValue())
                .conversionRate(conversionRate)
                .cancellationRate(cancellationRate)
                .retentionRate(retentionRate)
                .averageDaysToConvert(5.2) // TODO: Calcular real
                .build();
    }

    /**
     * Reporte de crecimiento
     */
    @Transactional(readOnly = true)
    public SubscriptionReportDTO.GrowthReport getGrowthReport(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        log.info("Generating growth report from {} to {}", startDate, endDate);

        // TODO: Implementar queries para obtener estos datos
        int newSubs = 28;
        int reactivations = 5;
        int cancellations = 7;
        int netGrowth = newSubs + reactivations - cancellations;

        long totalActive = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        double growthRate = totalActive > 0
                ? (netGrowth / (double) totalActive) * 100
                : 0.0;

        return SubscriptionReportDTO.GrowthReport.builder()
                .newSubscriptions(newSubs)
                .reactivations(reactivations)
                .cancellations(cancellations)
                .netGrowth(netGrowth)
                .growthRate(growthRate)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Map<String, Integer> getSubscriptionsByPlan() {
        List<Plan> plans = planRepository.findAll();
        Map<String, Integer> result = new HashMap<>();

        for (Plan plan : plans) {
            long count = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
            result.put(plan.getName(), (int) count);
        }

        return result;
    }

    private Map<String, BigDecimal> getRevenueByPlan() {
        Map<String, BigDecimal> result = new HashMap<>();

        // TODO: Implementar query para obtener ingresos por plan
        result.put("Free", BigDecimal.ZERO);
        result.put("Pro", BigDecimal.valueOf(5000));
        result.put("Business", BigDecimal.valueOf(12000));
        result.put("Enterprise", BigDecimal.valueOf(25000));

        return result;
    }

    private Double calculateGrowthRate(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: Implementar cálculo real de tasa de crecimiento
        return 18.3;
    }
}
