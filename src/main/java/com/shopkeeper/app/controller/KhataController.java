package com.shopkeeper.app.controller;

import com.shopkeeper.app.dto.KhataEntryRequest;
import com.shopkeeper.app.dto.KhataSettleRequest;
import com.shopkeeper.app.entity.KhataEntry;
import com.shopkeeper.app.service.KhataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khata")
@RequiredArgsConstructor
public class KhataController {

    private final KhataService khataService;

    // Add a new udhaar entry (YOU_GAVE = credit given to customer, YOU_GOT = payment received)
    @PostMapping
    public ResponseEntity<KhataEntry> addEntry(@Valid @RequestBody KhataEntryRequest req) {
        return ResponseEntity.ok(khataService.addEntry(req));
    }

    @GetMapping
    public ResponseEntity<List<KhataEntry>> listAll() {
        return ResponseEntity.ok(khataService.listAll());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<KhataEntry>> listPending() {
        return ResponseEntity.ok(khataService.listPending());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<KhataEntry>> listByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(khataService.listByCustomer(customerId));
    }

    // Settle (fully or partially) a pending udhaar entry; triggers a notification to the customer
    @PostMapping("/{id}/settle")
    public ResponseEntity<KhataEntry> settle(@PathVariable Long id, @Valid @RequestBody KhataSettleRequest req) {
        return ResponseEntity.ok(khataService.settle(id, req.getAmount()));
    }
}
