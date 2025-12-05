package com.taskmanager.dto;

import com.taskmanager.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

// ===================================
// EXPORT JOB DTOs
// ===================================

@Getter
@Setter
@NoArgsConstructor
public class ExportDTO {

    // Response DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long requestedById;
        private String requestedByEmail;
        private ExportType type;
        private ExportFormat format;
        private Long referenceId;
        private ExportStatus status;
        private String fileName;
        private Long fileSize;
        private String formattedFileSize;
        private Integer totalRecords;
        private Integer processedRecords;
        private Integer progress;
        private String downloadUrl;
        private LocalDateTime expiresAt;
        private Integer downloadCount;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
        private String estimatedTime;
        private Boolean isExpired;
        private Integer daysUntilExpiration;
    }

    // Create Request
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotNull(message = "Export type is required")
        private ExportType type;

        @NotNull(message = "Export format is required")
        private ExportFormat format;

        private Long referenceId;

        private Map<String, Object> filters;

        private Boolean includeComments;
        private Boolean includeAttachments;
        private Boolean includeActivities;
        private LocalDateTime dateFrom;
        private LocalDateTime dateTo;
    }
}
