package com.shopkeeper.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String sku; // stock keeping unit / item code

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(nullable = false)
    private String unit; // e.g. pcs, kg, litre

    @Column(nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    // triggers a low-stock flag when stockQuantity <= this
    @Builder.Default
    private Integer lowStockThreshold = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopkeeper_id", nullable = false)
    private User shopkeeper;

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public boolean isLowStock() {
        return stockQuantity != null && lowStockThreshold != null && stockQuantity <= lowStockThreshold;
    }
}
