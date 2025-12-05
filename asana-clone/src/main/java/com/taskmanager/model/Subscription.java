// ===================================
// SUBSCRIPTION ENTITY
// ===================================
package com.taskmanager.model;

import com.taskmanager.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscription_user", columnList = "user_id"),
        @Index(name = "idx_subscription_status", columnList = "status"),
        @Index(name = "idx_subscription_end_date", columnList = "end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    // ESTADO DE LA SUSCRIPCIÓN
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.TRIAL;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    private LocalDateTime trialEndDate;

    private LocalDateTime cancelledAt;

    // BILLING
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingPeriod billingPeriod = BillingPeriod.MONTHLY;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    private LocalDateTime nextBillingDate;

    @Column(nullable = false)
    private Boolean autoRenew = true;

    // PAGOS (Integración Stripe)
    @Column(length = 100)
    private String stripeSubscriptionId;

    @Column(length = 100)
    private String stripeCustomerId;

    @Column(length = 50)
    private String paymentMethod; // "CREDIT_CARD", "PAYPAL", "BANK_TRANSFER"

    // MÉTRICAS DE USO
    @Column(nullable = false)
    private Integer currentProjects = 0;

    @Column(nullable = false)
    private Integer currentMembers = 0;

    @Column(nullable = false)
    private Long currentStorageUsed = 0L; // Bytes

    // HISTORIAL
    private LocalDateTime lastPaymentDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer renewalCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}