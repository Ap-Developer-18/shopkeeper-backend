package com.shopkeeper.app.controller;

import com.shopkeeper.app.dto.DashboardOverviewResponse;
import com.shopkeeper.app.repository.CustomerRepository;
import com.shopkeeper.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        long totalProducts = productRepository.count();
        long totalCustomers = customerRepository.count();

        DashboardOverviewResponse overview = DashboardOverviewResponse.builder()
                .todaySales(BigDecimal.ZERO)
                .totalSales(BigDecimal.ZERO)
                .pendingUdhaar(BigDecimal.ZERO)
                .pendingKhata(BigDecimal.ZERO)
                .lowStockCount(0L)
                .totalBills(0L)
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Dashboard overview fetched successfully");
        response.put("data", overview);

        return ResponseEntity.ok(response);
    }
}