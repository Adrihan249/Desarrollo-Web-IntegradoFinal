package com.taskmanager.dto;

// ===================================
// DTOs DEL SPRINT 4
// Ubicación: com.taskmanager.dto
// ===================================

import com.taskmanager.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

// ===================================
// PLAN DTOs
// ===================================

@Getter
@Setter
@NoArgsConstructor
public class PlanDTO {

    // Response DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private BigDecimal annualPrice;
        private Integer maxProjects;
        private Integer maxMembers;
        private Integer maxStorage;
        private Integer maxAttachmentSize;
        private Boolean customFields;
        private Boolean timeline;
        private Boolean ganttChart;
        private Boolean advancedReports;
        private Boolean prioritySupport;
        private Boolean apiAccess;
        private Boolean customBranding;
        private Boolean ssoEnabled;
        private Boolean active;
        private Integer trialDays;
        private Long activeSubscriptions;
        private LocalDateTime createdAt;
    }

    // Create/Update Request DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Plan name is required")
        @Size(max = 50)
        private String name;

        @Size(max = 500)
        private String description;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = true)
        private BigDecimal price;

        @DecimalMin(value = "0.0", inclusive = true)
        private BigDecimal annualPrice;

        @NotNull private Integer maxProjects;
        @NotNull private Integer maxMembers;
        @NotNull private Integer maxStorage;
        @NotNull private Integer maxAttachmentSize;

        private Boolean customFields;
        private Boolean timeline;
        private Boolean ganttChart;
        private Boolean advancedReports;
        private Boolean prioritySupport;
        private Boolean apiAccess;
        private Boolean customBranding;
        private Boolean ssoEnabled;
        private Integer trialDays;
    }
}
