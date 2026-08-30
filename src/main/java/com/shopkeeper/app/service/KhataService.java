package com.shopkeeper.app.service;

import com.shopkeeper.app.dto.KhataEntryRequest;
import com.shopkeeper.app.entity.*;
import com.shopkeeper.app.exception.ApiException;
import com.shopkeeper.app.repository.KhataEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhataService {

    private final KhataEntryRepository khataEntryRepository;
    private final CustomerService customerService;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public KhataEntry addEntry(KhataEntryRequest req) {
        User shopkeeper = currentUserService.getCurrentUser();
        Customer customer = customerService.getById(req.getCustomerId());

        KhataEntry.EntryType type = KhataEntry.EntryType.valueOf(req.getType().toUpperCase());
        LocalDate dueDate = req.getDueDate() != null ? LocalDate.parse(req.getDueDate()) : null;

        KhataEntry entry = KhataEntry.builder()
                .customer(customer)
                .shopkeeper(shopkeeper)
                .type(type)
                .amount(req.getAmount())
                .balanceRemaining(type == KhataEntry.EntryType.YOU_GAVE ? req.getAmount() : BigDecimal.ZERO)
                .note(req.getNote())
                .dueDate(dueDate)
                .build();

        return khataEntryRepository.save(entry);
    }

    /** Creates a khata (udhaar) entry linked to a bill for an unpaid/partial amount. */
    public KhataEntry addFromBill(Bill bill, BigDecimal pendingAmount, LocalDate dueDate) {
        KhataEntry entry = KhataEntry.builder()
                .customer(bill.getCustomer())
                .shopkeeper(bill.getShopkeeper())
                .bill(bill)
                .type(KhataEntry.EntryType.YOU_GAVE)
                .amount(pendingAmount)
                .balanceRemaining(pendingAmount)
                .note("Pending balance for bill " + bill.getBillNumber())
                .dueDate(dueDate)
                .build();
        return khataEntryRepository.save(entry);
    }

    public List<KhataEntry> listAll() {
        User shopkeeper = currentUserService.getCurrentUser();
        return khataEntryRepository.findByShopkeeperId(shopkeeper.getId());
    }

    public List<KhataEntry> listPending() {
        User shopkeeper = currentUserService.getCurrentUser();
        return khataEntryRepository.findByShopkeeperIdAndStatusNot(
                shopkeeper.getId(), KhataEntry.KhataStatus.SETTLED);
    }

    public List<KhataEntry> listByCustomer(Long customerId) {
        return khataEntryRepository.findByCustomerId(customerId);
    }

    public KhataEntry settle(Long entryId, BigDecimal amount) {
        KhataEntry entry = khataEntryRepository.findById(entryId)
                .orElseThrow(() -> new ApiException("Khata entry not found"));
        ensureOwnership(entry);

        BigDecimal newBalance = entry.getBalanceRemaining().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
            entry.setBalanceRemaining(BigDecimal.ZERO);
            entry.setStatus(KhataEntry.KhataStatus.SETTLED);
        } else {
            entry.setBalanceRemaining(newBalance);
            entry.setStatus(KhataEntry.KhataStatus.PARTIALLY_SETTLED);
        }

        KhataEntry saved = khataEntryRepository.save(entry);

        String message = "Payment of Rs." + amount + " received. Remaining balance: Rs."
                + saved.getBalanceRemaining();
        notificationService.notifyBoth(entry.getCustomer().getPhone(), message);

        return saved;
    }

    /** Used by the scheduled reminder job. */
    public List<KhataEntry> findDueForReminder(LocalDate today) {
        return khataEntryRepository.findByStatusNotAndDueDateLessThanEqualAndReminderSentFalse(
                KhataEntry.KhataStatus.SETTLED, today);
    }

    public void markReminderSent(KhataEntry entry) {
        entry.setReminderSent(true);
        khataEntryRepository.save(entry);
    }

    private void ensureOwnership(KhataEntry entry) {
        User current = currentUserService.getCurrentUser();
        if (!entry.getShopkeeper().getId().equals(current.getId())) {
            throw new ApiException("Not authorized to access this khata entry");
        }
    }
}
