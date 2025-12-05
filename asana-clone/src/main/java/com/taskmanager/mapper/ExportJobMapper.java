package com.taskmanager.mapper;

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
// ===================================
// EXPORT JOB MAPPER
// ===================================
@Mapper(componentModel = "spring")
@Component
public interface ExportJobMapper {

    @Mapping(source = "requestedBy.id", target = "requestedById")
    @Mapping(source = "requestedBy.email", target = "requestedByEmail")
    @Mapping(target = "formattedFileSize", expression = "java(formatFileSize(exportJob.getFileSize()))")
    @Mapping(target = "estimatedTime", expression = "java(estimateTime(exportJob.getTotalRecords(), exportJob.getStatus()))")
    @Mapping(target = "isExpired", expression = "java(isExpired(exportJob.getExpiresAt()))")
    @Mapping(target = "daysUntilExpiration", expression = "java(calculateDaysUntilExpiration(exportJob.getExpiresAt()))")
    ExportDTO.Response toResponse(ExportJob exportJob);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestedBy", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "filePath", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "totalRecords", ignore = true)
    @Mapping(target = "processedRecords", constant = "0")
    @Mapping(target = "progress", constant = "0")
    @Mapping(target = "downloadUrl", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "downloadCount", constant = "0")
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "filters", ignore = true)
    ExportJob toEntity(ExportDTO.CreateRequest request);

    default String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";

        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    default String estimateTime(Integer totalRecords, com.taskmanager.model.enums.ExportStatus status) {
        if (status == com.taskmanager.model.enums.ExportStatus.COMPLETED) return "Completed";
        if (status == com.taskmanager.model.enums.ExportStatus.FAILED) return "Failed";
        if (totalRecords == null || totalRecords == 0) return "2-3 minutes";

        // Estimar basado en cantidad de registros (aprox 100 registros por segundo)
        int estimatedSeconds = totalRecords / 100;
        if (estimatedSeconds < 60) return "Less than 1 minute";
        if (estimatedSeconds < 180) return "2-3 minutes";
        if (estimatedSeconds < 300) return "3-5 minutes";
        return "5-10 minutes";
    }

    default Boolean isExpired(LocalDateTime expiresAt) {
        if (expiresAt == null) return false;
        return expiresAt.isBefore(LocalDateTime.now());
    }

    default Integer calculateDaysUntilExpiration(LocalDateTime expiresAt) {
        if (expiresAt == null) return null;
        LocalDateTime now = LocalDateTime.now();
        if (expiresAt.isBefore(now)) return 0;
        return (int) ChronoUnit.DAYS.between(now, expiresAt);
    }
}
