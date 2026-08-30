package com.shopkeeper.app.controller;

import com.shopkeeper.app.dto.CreateBillRequest;
import com.shopkeeper.app.entity.Bill;
import com.shopkeeper.app.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    // Creates a bill: item * number = total is computed automatically per line and summed.
    @PostMapping
    public ResponseEntity<Bill> create(@Valid @RequestBody CreateBillRequest req) {
        return ResponseEntity.ok(billService.createBill(req));
    }

    @GetMapping
    public ResponseEntity<List<Bill>> listAll() {
        return ResponseEntity.ok(billService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Bill>> listByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(billService.listByCustomer(customerId));
    }
}
