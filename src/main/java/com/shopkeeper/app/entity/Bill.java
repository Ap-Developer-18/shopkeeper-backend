package com.shopkeeper.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String billNumber; // human readable, e.g. INV-000123

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopkeeper_id", nullable = false)
    @JsonIgnore
    private User shopkeeper;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<BillItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BillStatus status = BillStatus.UNPAID; // UNPAID, PAID, PARTIALLY_PAID (udhaar)

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonProperty("customerName")
    public String getCustomerName() {
        return customer != null ? customer.getName() : null;
    }

    @JsonProperty("total")
    public BigDecimal getTotal() {
        return totalAmount;
    }

    @JsonProperty("paymentMode")
    public String getPaymentMode() {
        return status != null ? status.name() : "CASH";
    }

    public enum BillStatus {
        UNPAID, PAID, PARTIALLY_PAID
    }
}