package com.shopkeeper.app.service;

import com.shopkeeper.app.entity.KhataEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Runs daily (cron configurable via app.khata.reminder-cron) and sends
 * SMS + WhatsApp reminders to customers whose udhaar khata balance is due
 * today or overdue.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KhataReminderScheduler {

    private final KhataService khataService;
    private final NotificationService notificationService;

    @Scheduled(cron = "${app.khata.reminder-cron}")
    public void sendPendingBalanceReminders() {
        LocalDate today = LocalDate.now();
        List<KhataEntry> dueEntries = khataService.findDueForReminder(today);

        log.info("Khata reminder job: found {} due entries", dueEntries.size());

        for (KhataEntry entry : dueEntries) {
            String message = "Reminder: You have a pending balance of Rs." + entry.getBalanceRemaining()
                    + " at " + entry.getShopkeeper().getCompanyName()
                    + (entry.getDueDate() != null ? " (due " + entry.getDueDate() + ")" : "")
                    + ". Please clear it at your earliest convenience.";

            notificationService.notifyBoth(entry.getCustomer().getPhone(), message);
            khataService.markReminderSent(entry);
        }
    }
}
