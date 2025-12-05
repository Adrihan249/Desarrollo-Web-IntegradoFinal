package com.taskmanager.mapper;

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
// ===================================
// SUBSCRIPTION MAPPER
// ===================================
@Mapper(componentModel = "spring", uses = {PlanMapper.class})
@Component
public interface SubscriptionMapper {

    // ✅ CORRECCIÓN 1: Usar expresión segura para userId
    @Mapping(target = "userId", expression = "java(subscription.getUser() != null ? subscription.getUser().getId() : null)")

    // ✅ CORRECCIÓN 2: Usar expresión segura para userEmail
    @Mapping(target = "userEmail", expression = "java(subscription.getUser() != null ? subscription.getUser().getEmail() : null)")

    @Mapping(target = "formattedStorageUsed", expression = "java(formatStorage(subscription.getCurrentStorageUsed()))")
    @Mapping(target = "daysRemaining", expression = "java(calculateDaysRemaining(subscription.getEndDate()))")
    @Mapping(target = "isExpiringSoon", expression = "java(isExpiringSoon(subscription.getEndDate()))")
    SubscriptionDTO.Response toResponse(Subscription subscription);
    default String formatStorage(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    default Integer calculateDaysRemaining(LocalDateTime endDate) {
        if (endDate == null) return null;
        LocalDateTime now = LocalDateTime.now();
        if (endDate.isBefore(now)) return 0;
        return (int) ChronoUnit.DAYS.between(now, endDate);
    }

    default Boolean isExpiringSoon(LocalDateTime endDate) {
        if (endDate == null) return false;
        Integer daysRemaining = calculateDaysRemaining(endDate);
        return daysRemaining != null && daysRemaining <= 7;
    }
}