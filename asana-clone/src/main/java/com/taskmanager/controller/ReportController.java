package com.taskmanager.controller;

import com.taskmanager.dto.*;
import com.taskmanager.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
// ===================================
// REPORT CONTROLLER
// ===================================
@RestController
@RequestMapping("/api/reports/subscription")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    /**
     * Dashboard de métricas
     */
    @GetMapping("/dashboard")
    public ResponseEntity<SubscriptionReportDTO.Dashboard> getDashboard() {
        log.info("GET /api/reports/subscription/dashboard");
        SubscriptionReportDTO.Dashboard dashboard = reportService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Reporte de crecimiento
     */
    @GetMapping("/growth")
    public ResponseEntity<SubscriptionReportDTO.GrowthReport> getGrowthReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("GET /api/reports/subscription/growth - From: {} To: {}", startDate, endDate);
        SubscriptionReportDTO.GrowthReport report = reportService
                .getGrowthReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
