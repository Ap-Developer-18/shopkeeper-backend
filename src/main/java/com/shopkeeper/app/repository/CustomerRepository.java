package com.shopkeeper.app.repository;

import com.shopkeeper.app.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByShopkeeperId(Long shopkeeperId);
    List<Customer> findByShopkeeperIdAndNameContainingIgnoreCase(Long shopkeeperId, String name);
}
