package com.taskmanager.dto;

import com.taskmanager.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
// ===================================
// SUBSCRIPTION DTOs
// ===================================

@Getter
@Setter
@NoArgsConstructor
public class SubscriptionDTO {

    // Response DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private String userEmail;
        private PlanDTO.Response plan;
        private SubscriptionStatus status;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LocalDateTime trialEndDate;
        private BillingPeriod billingPeriod;
        private BigDecimal amount;
        private String currency;
        private LocalDateTime nextBillingDate;
        private Boolean autoRenew;
        private Integer currentProjects;
        private Integer currentMembers;
        private Long currentStorageUsed;
        private String formattedStorageUsed;
        private LocalDateTime lastPaymentDate;
        private BigDecimal totalPaid;
        private Integer renewalCount;
        private Integer daysRemaining;
        private Boolean isExpiringSoon;
        private LocalDateTime createdAt;
    }

    // Create Subscription Request
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotNull(message = "Plan ID is required")
        private Long planId;

        @NotNull(message = "Billing period is required")
        private BillingPeriod billingPeriod;

        @NotBlank(message = "Payment method is required")
        private String paymentMethod;

        private String stripeToken;
    }

    // Change Plan Request
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangePlanRequest {
        @NotNull(message = "New plan ID is required")
        private Long newPlanId;

        @NotNull(message = "Billing period is required")
        private BillingPeriod billingPeriod;

        // 💡 CORRECCIÓN: Añadir el método de pago para cambios inmediatos
        @NotBlank(message = "Payment method is required for immediate change")
        private String paymentMethod;

        private Boolean immediate;
    }

    // Cancel Request
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CancelRequest {
        @Size(max = 500)
        private String reason;

        private Boolean immediate;
    }

    // Usage Summary
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsageSummary {
        private UsageMetric projects;
        private UsageMetric members;
        private UsageMetric storage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsageMetric {
        private Integer used;
        private Integer limit;
        private Integer percentage;
        private Boolean isNearLimit;
        private Boolean isOverLimit;
    }
}
