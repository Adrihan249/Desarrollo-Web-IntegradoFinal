package com.taskmanager.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class SubscriptionReportDTO {

    // Dashboard Metrics
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Dashboard {
        private MetricsSummary summary;
        private RevenueMetrics revenue;
        private ConversionMetrics conversions;
        private Map<String, Integer> subscriptionsByPlan;
        private Map<String, BigDecimal> revenueByPlan;
        private LocalDateTime generatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetricsSummary {
        private Integer totalSubscriptions;
        private Integer activeSubscriptions;
        private Integer trialSubscriptions;
        private Integer cancelledSubscriptions;
        private Integer suspendedSubscriptions;
        private Double growthRate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenueMetrics {
        private BigDecimal monthlyRevenue;
        private BigDecimal annualRevenue;
        private BigDecimal totalRevenue;
        private BigDecimal averageRevenuePerUser;
        private BigDecimal monthlyRecurringRevenue;
        private BigDecimal annualRecurringRevenue;
        private BigDecimal projectedMonthlyRevenue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConversionMetrics {
        private Integer trialStarted;
        private Integer trialConverted;
        private Double conversionRate;
        private Double cancellationRate;
        private Double retentionRate;
        private Double averageDaysToConvert;
    }

    // Growth Report
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GrowthReport {
        private Integer newSubscriptions;
        private Integer reactivations;
        private Integer cancellations;
        private Integer netGrowth;
        private Double growthRate;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}
