package com.shopkeeper.app.repository;

import com.shopkeeper.app.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByShopkeeperId(Long shopkeeperId);
    List<Bill> findByCustomerId(Long customerId);
    Optional<Bill> findByBillNumber(String billNumber);
    long countByShopkeeperId(Long shopkeeperId);
}
