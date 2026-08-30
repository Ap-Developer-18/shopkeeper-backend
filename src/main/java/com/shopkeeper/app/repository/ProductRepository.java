package com.shopkeeper.app.repository;

import com.shopkeeper.app.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopkeeperId(Long shopkeeperId);
    List<Product> findByShopkeeperIdAndStockQuantityLessThanEqual(Long shopkeeperId, Integer threshold);
}
