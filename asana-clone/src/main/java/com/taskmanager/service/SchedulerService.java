package com.taskmanager.service;

import com.taskmanager.model.*;
import com.taskmanager.model.enums.*;
import com.taskmanager.Repositorios.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
// ===================================
// SCHEDULER SERVICE
// ===================================
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final ReminderRepository reminderRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ReminderService reminderService;
    private final NotificationService notificationService;

    /**
     * Procesar recordatorios pendientes
     * Se ejecuta cada 5 minutos
     */
    @Transactional
    public void processReminders() {
        log.info("Processing pending reminders...");

        LocalDateTime now = LocalDateTime.now();
        List<Reminder> pendingReminders = reminderRepository.findPendingReminders(now);

        log.info("Found {} reminders to process", pendingReminders.size());

        for (Reminder reminder : pendingReminders) {
            reminderService.sendReminder(reminder);
        }
    }

    /**
     * Verificar suscripciones que vencen pronto
     * Se ejecuta diariamente a las 10:00 AM
     */
    @Transactional
    public void checkExpiringSubscriptions() {
        log.info("Checking for expiring subscriptions...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);

        List<Subscription> expiringSubscriptions = subscriptionRepository
                .findExpiringSubscriptions(now, sevenDaysLater);

        log.info("Found {} expiring subscriptions", expiringSubscriptions.size());

        for (Subscription subscription : expiringSubscriptions) {
            // Crear notificación de renovación
            notificationService.createSubscriptionExpiryNotification(
                    subscription.getUser(),
                    subscription.getEndDate()
            );
        }
    }

    /**
     * Verificar trials que terminan pronto
     * Se ejecuta diariamente a las 9:00 AM
     */
    @Transactional
    public void checkEndingTrials() {
        log.info("Checking for ending trials...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysLater = now.plusDays(3);

        List<Subscription> endingTrials = subscriptionRepository
                .findTrialsEndingSoon(now, threeDaysLater);

        log.info("Found {} trials ending soon", endingTrials.size());

        for (Subscription subscription : endingTrials) {
            // Crear notificación de fin de trial
            notificationService.createTrialEndingNotification(
                    subscription.getUser(),
                    subscription.getTrialEndDate()
            );
        }
    }

    /**
     * Procesar renovaciones automáticas
     * Se ejecuta diariamente a las 2:00 AM
     */
    @Transactional
    public void processAutoRenewals() {
        log.info("Processing auto-renewals...");

        LocalDateTime now = LocalDateTime.now();
        List<Subscription> subscriptionsToRenew = subscriptionRepository
                .findSubscriptionsRequiringPayment(now);

        log.info("Found {} subscriptions to renew", subscriptionsToRenew.size());

        for (Subscription subscription : subscriptionsToRenew) {
            try {
                // TODO: Procesar pago con Stripe
                // paymentService.processPayment(subscription);

                // Actualizar fechas
                LocalDateTime newEndDate = subscription.getBillingPeriod() == BillingPeriod.ANNUAL
                        ? subscription.getEndDate().plusYears(1)
                        : subscription.getEndDate().plusMonths(1);

                subscription.setEndDate(newEndDate);
                subscription.setNextBillingDate(newEndDate);
                subscription.setLastPaymentDate(now);
                subscription.setRenewalCount(subscription.getRenewalCount() + 1);
                subscription.setTotalPaid(
                        subscription.getTotalPaid().add(subscription.getAmount())
                );

                subscriptionRepository.save(subscription);

                log.info("Subscription renewed successfully for user: {}",
                        subscription.getUser().getEmail());

            } catch (Exception e) {
                log.error("Failed to renew subscription for user: {}",
                        subscription.getUser().getEmail(), e);

                // Marcar como suspendida si falla el pago
                subscription.setStatus(SubscriptionStatus.SUSPENDED);
                subscriptionRepository.save(subscription);
            }
        }
    }

    /**
     * Limpiar recordatorios antiguos
     * Se ejecuta semanalmente (Domingo a las 3:00 AM)
     */
    @Transactional
    public void cleanupOldReminders() {
        log.info("Cleaning up old reminders...");

        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<Reminder> oldReminders = reminderRepository.findOverdueReminders(threshold);

        log.info("Found {} old reminders to clean", oldReminders.size());

        for (Reminder reminder : oldReminders) {
            if (reminder.getFrequency() == ReminderFrequency.ONCE &&
                    reminder.getStatus() == ReminderStatus.SENT) {
                reminderRepository.delete(reminder);
            }
        }

        log.info("Old reminders cleanup completed");
    }
}