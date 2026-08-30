package com.shopkeeper.app.controller;

import com.shopkeeper.app.dto.PaymentRequest;
import com.shopkeeper.app.entity.Payment;
import com.shopkeeper.app.service.BarcodeService;
import com.shopkeeper.app.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final BarcodeService barcodeService;

    // Records a UPI or CASH payment against a bill; auto-generates a barcode
    // and pushes any remaining balance into the udhaar khata book.
    @PostMapping
    public ResponseEntity<Payment> recordPayment(@Valid @RequestBody PaymentRequest req) {
        return ResponseEntity.ok(paymentService.recordPayment(req));
    }

    @GetMapping("/bill/{billId}")
    public ResponseEntity<Payment> getByBill(@PathVariable Long billId) {
        return ResponseEntity.ok(paymentService.getByBillId(billId));
    }

    @GetMapping("/bill/{billId}/barcode")
    public ResponseEntity<Resource> getBarcode(@PathVariable Long billId) {
        Payment payment = paymentService.getByBillId(billId);
        Resource resource = new FileSystemResource(barcodeService.getBarcodeFile(payment.getBarcodeImagePath()));
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }
}
