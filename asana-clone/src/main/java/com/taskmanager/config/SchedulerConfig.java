package com.taskmanager.config;

// ===================================
// CONFIGURACIONES DEL SPRINT 4
// Ubicación: com.taskmanager.config
// ===================================

import com.taskmanager.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

// ===================================
// SCHEDULER CONFIG
// ===================================
@Configuration
@EnableScheduling
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    private final SchedulerService schedulerService;

    /**
     * Task Scheduler para tareas programadas
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();

        log.info("Task Scheduler initialized with pool size: 10");
        return scheduler;
    }

    /**
     * Task Executor para tareas asíncronas
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.setAwaitTerminationSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();

        log.info("Async Executor initialized with core pool size: 5");
        return executor;
    }

    // ==================== TAREAS PROGRAMADAS ====================

    /**
     * Procesar recordatorios pendientes
     * Se ejecuta cada 5 minutos
     */
    @Scheduled(fixedRate = 300000) // 5 minutos = 300,000 ms
    public void processReminders() {
        log.debug("Running scheduled task: processReminders");
        schedulerService.processReminders();
    }

    /**
     * Verificar trials que terminan pronto
     * Se ejecuta diariamente a las 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkEndingTrials() {
        log.info("Running scheduled task: checkEndingTrials");
        schedulerService.checkEndingTrials();
    }

    /**
     * Verificar suscripciones que vencen pronto
     * Se ejecuta diariamente a las 10:00 AM
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void checkExpiringSubscriptions() {
        log.info("Running scheduled task: checkExpiringSubscriptions");
        schedulerService.checkExpiringSubscriptions();
    }

    /**
     * Procesar renovaciones automáticas
     * Se ejecuta diariamente a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void processAutoRenewals() {
        log.info("Running scheduled task: processAutoRenewals");
        schedulerService.processAutoRenewals();
    }

    /**
     * Limpiar recordatorios antiguos
     * Se ejecuta semanalmente (Domingo a las 3:00 AM)
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void cleanupOldReminders() {
        log.info("Running scheduled task: cleanupOldReminders");
        schedulerService.cleanupOldReminders();
    }
}