package com.shopkeeper.app.repository;

import com.shopkeeper.app.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBillId(Long billId);
}
