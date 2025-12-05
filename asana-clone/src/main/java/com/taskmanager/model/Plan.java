// ===================================
// ENTIDADES DEL SPRINT 4
// Ubicación: com.taskmanager.model
// ===================================

package com.taskmanager.model;

import com.taskmanager.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ===================================
// PLAN ENTITY
// ===================================
@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // "Free", "Pro", "Business", "Enterprise"

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Precio mensual

    @Column(precision = 10, scale = 2)
    private BigDecimal annualPrice; // Precio anual (con descuento)

    // LÍMITES DEL PLAN (-1 = ilimitado)
    @Column(nullable = false)
    private Integer maxProjects = -1;

    @Column(nullable = false)
    private Integer maxMembers = -1;

    @Column(nullable = false)
    private Integer maxStorage = -1; // MB

    @Column(nullable = false)
    private Integer maxAttachmentSize = 10; // MB por archivo

    // FEATURES
    @Column(nullable = false)
    private Boolean customFields = false;

    @Column(nullable = false)
    private Boolean timeline = false;

    @Column(nullable = false)
    private Boolean ganttChart = false;

    @Column(nullable = false)
    private Boolean advancedReports = false;

    @Column(nullable = false)
    private Boolean prioritySupport = false;

    @Column(nullable = false)
    private Boolean apiAccess = false;

    @Column(nullable = false)
    private Boolean customBranding = false;

    @Column(nullable = false)
    private Boolean ssoEnabled = false; // Single Sign-On

    // METADATA
    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Integer trialDays = 14; // Días de prueba gratis

    @Column(length = 100)
    private String stripePriceId; // ID de Stripe (integración futura)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}