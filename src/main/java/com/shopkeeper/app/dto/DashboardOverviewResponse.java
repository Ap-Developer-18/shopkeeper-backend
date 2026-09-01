package com.shopkeeper.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {
    @Builder.Default
    private BigDecimal todaySales = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalSales = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal pendingUdhaar = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal pendingKhata = BigDecimal.ZERO;

    @Builder.Default
    private Long lowStockCount = 0L;

    @Builder.Default
    private Long totalBills = 0L;

    @Builder.Default
    private Long totalCustomers = 0L;

    @Builder.Default
    private Long totalProducts = 0L;
}