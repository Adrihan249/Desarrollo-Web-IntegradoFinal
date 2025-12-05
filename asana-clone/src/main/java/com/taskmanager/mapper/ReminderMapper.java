package com.taskmanager.mapper;

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
// ===================================
// REMINDER MAPPER
// ===================================
@Mapper(componentModel = "spring")
@Component
public interface ReminderMapper {

    @Mapping(target = "userId", expression = "java(reminder.getUser() != null ? reminder.getUser().getId() : null)") // ✅ CORRECCIÓN
    @Mapping(target = "isOverdue", expression = "java(isOverdue(reminder.getReminderDate(), reminder.getStatus()))")
    @Mapping(target = "timeUntilReminder", expression = "java(formatTimeUntil(reminder.getReminderDate()))")
    ReminderDTO.Response toResponse(Reminder reminder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "dismissedAt", ignore = true)
    @Mapping(target = "snoozeUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Reminder toEntity(ReminderDTO.CreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "referenceId", ignore = true)
    @Mapping(target = "referenceType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "dismissedAt", ignore = true)
    @Mapping(target = "snoozeUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ReminderDTO.UpdateRequest request, @MappingTarget Reminder reminder);

    default Boolean isOverdue(LocalDateTime reminderDate, com.taskmanager.model.enums.ReminderStatus status) {
        if (reminderDate == null || status != com.taskmanager.model.enums.ReminderStatus.PENDING) {
            return false;
        }
        return reminderDate.isBefore(LocalDateTime.now());
    }

    default String formatTimeUntil(LocalDateTime reminderDate) {
        if (reminderDate == null) return null;

        LocalDateTime now = LocalDateTime.now();
        if (reminderDate.isBefore(now)) return "Overdue";

        Duration duration = Duration.between(now, reminderDate);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        if (hours > 24) {
            long days = hours / 24;
            return String.format("%d day%s", days, days > 1 ? "s" : "");
        } else if (hours > 0) {
            return String.format("%d hour%s", hours, hours > 1 ? "s" : "");
        } else {
            return String.format("%d minute%s", minutes, minutes != 1 ? "s" : "");
        }
    }
}