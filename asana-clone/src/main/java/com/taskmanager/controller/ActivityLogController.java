package com.taskmanager.controller;

import com.taskmanager.dto.*;
import com.taskmanager.model.User;
import com.taskmanager.service.ChatMessageService;
import com.taskmanager.service.ActivityLogService;
import com.taskmanager.service.FilterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
/**
 * ===================================================================
 * Controlador REST de Actividades (Timeline)
 *
 * CUMPLE REQUERIMIENTO N°7: Seguimiento de avances
 * ===================================================================
 */
@RestController
@RequestMapping("/api/projects/{projectId}/activity")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
class ActivityLogController {

    private final ActivityLogService activityLogService;

    /**
     * N°7: Obtiene timeline del proyecto
     * GET /api/projects/{projectId}/activity?page=0&size=50
     */
    @GetMapping
    public ResponseEntity<List<ActivityLogDTO.Response>> getProjectTimeline(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/activity?page={}&size={}", projectId, page, size);

        List<ActivityLogDTO.Response> activities = activityLogService
                .getProjectTimeline(projectId, currentUser.getId(), page, size);

        return ResponseEntity.ok(activities);
    }

    /**
     * N°7: Obtiene timeline agrupado por fecha
     * GET /api/projects/{projectId}/activity/timeline
     */
    @GetMapping("/timeline")
    public ResponseEntity<List<ActivityLogDTO.Timeline>> getTimelineGrouped(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/activity/timeline", projectId);

        List<ActivityLogDTO.Timeline> timeline = activityLogService
                .getTimelineGroupedByDate(projectId, currentUser.getId());

        return ResponseEntity.ok(timeline);
    }

    /**
     * N°7: Obtiene estadísticas de actividad
     * GET /api/projects/{projectId}/activity/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ActivityLogDTO.Summary> getActivityStats(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/activity/stats", projectId);

        ActivityLogDTO.Summary summary = activityLogService.getActivitySummary(projectId);

        return ResponseEntity.ok(summary);
    }

    /**
     * N°7: Obtiene actividades de un usuario
     * GET /api/projects/{projectId}/activity/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ActivityLogDTO.Response>> getUserActivities(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{}/activity/user/{}", projectId, userId);

        List<ActivityLogDTO.Response> activities = activityLogService
                .getUserActivities(projectId, userId);

        return ResponseEntity.ok(activities);
    }
}
