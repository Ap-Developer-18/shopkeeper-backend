package com.shopkeeper.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false, unique = true)
    private Bill bill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode mode; // UPI or CASH

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    // Sender (customer paying) details
    private String senderName;
    private String senderUpiId;
    private String senderPhone;

    // Receiver (shopkeeper) details
    private String receiverName;
    private String receiverUpiId;
    private String receiverPhone;

    private String transactionRef; // UPI transaction ref / cash receipt no.

    private String barcodeImagePath; // path/url to generated barcode image for this bill payment

    @Builder.Default
    private LocalDateTime paidAt = LocalDateTime.now();

    public enum PaymentMode {
        UPI, CASH
    }
}
