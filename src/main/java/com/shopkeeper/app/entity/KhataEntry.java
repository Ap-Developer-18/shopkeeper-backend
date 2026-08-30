package com.shopkeeper.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Udhaar Khata (credit book) entry.
 * Tracks money a customer owes the shopkeeper (or vice versa), with a due
 * date so the scheduler can send pending-balance reminders.
 */
@Entity
@Table(name = "khata_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhataEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopkeeper_id", nullable = false)
    private User shopkeeper;

    // Optional link to originating bill, if the udhaar came from a bill
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type; // YOU_GAVE (credit given to customer) / YOU_GOT (payment received)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceRemaining; // pending amount still to be collected

    private String note;

    private LocalDate dueDate; // used to trigger pending-balance notification

    @Builder.Default
    private boolean reminderSent = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private KhataStatus status = KhataStatus.PENDING;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum EntryType {
        YOU_GAVE, YOU_GOT
    }

    public enum KhataStatus {
        PENDING, PARTIALLY_SETTLED, SETTLED
    }
}
