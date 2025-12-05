package com.taskmanager.mapper;

// ===================================
// MAPPERS DEL SPRINT 4
// Ubicación: com.taskmanager.mapper
// ===================================

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

// ===================================
// PLAN MAPPER
// ===================================
@Mapper(componentModel = "spring")
@Component
public interface PlanMapper {

    @Mapping(target = "activeSubscriptions", ignore = true)
    PlanDTO.Response toResponse(Plan plan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "stripePriceId", ignore = true)
    Plan toEntity(PlanDTO.CreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PlanDTO.CreateRequest request, @MappingTarget Plan plan);
}