package com.shopkeeper.app.service;

import com.shopkeeper.app.dto.ProductRequest;
import com.shopkeeper.app.entity.Product;
import com.shopkeeper.app.entity.User;
import com.shopkeeper.app.exception.ApiException;
import com.shopkeeper.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

 public Product addProduct(ProductRequest req) {
    User shopkeeper = currentUserService.getCurrentUser();
    Product product = Product.builder()
            .name(req.getName())
            .sku(req.getSku())
            .pricePerUnit(req.getPricePerUnit())
            .unit(req.getUnit())
            .stockQuantity(req.getStockQuantity())
            .lowStockThreshold(req.getLowStockThreshold() != null ? req.getLowStockThreshold() : 5)
            .shopkeeper(shopkeeper)
            .build();
    return productRepository.save(product);
}

    @Transactional(readOnly = true)
    public List<Product> listAll() {
        User shopkeeper = currentUserService.getCurrentUser();
        return productRepository.findByShopkeeperId(shopkeeper.getId());
    }

    @Transactional(readOnly = true)
    public List<Product> lowStockItems() {
        User shopkeeper = currentUserService.getCurrentUser();
        return productRepository.findByShopkeeperId(shopkeeper.getId()).stream()
                .filter(Product::isLowStock)
                .toList();
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ApiException("Product not found with id: " + id));
        ensureOwnership(p);
        return p;
    }

    public Product update(Long id, ProductRequest req) {
        Product p = getById(id);
        p.setName(req.getName());
        p.setSku(req.getSku());
        p.setPricePerUnit(req.getPricePerUnit());
        p.setUnit(req.getUnit());
        p.setStockQuantity(req.getStockQuantity());
        if (req.getLowStockThreshold() != null) {
            p.setLowStockThreshold(req.getLowStockThreshold());
        }
        p.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(p);
    }

    /** Adjusts stock up (restock) or down (sale); throws if it would go negative. */
    public Product adjustStock(Long id, int delta) {
        Product p = getById(id);
        int newQty = p.getStockQuantity() + delta;
        if (newQty < 0) {
            throw new ApiException("Insufficient stock for product: " + p.getName());
        }
        p.setStockQuantity(newQty);
        p.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(p);
    }

    public void delete(Long id) {
        Product p = getById(id);
        productRepository.delete(p);
    }

    private void ensureOwnership(Product p) {
        if (p.getShopkeeper() == null) {
            return;
        }
        User current = currentUserService.getCurrentUser();
        if (current != null && current.getId() != null && !p.getShopkeeper().getId().equals(current.getId())) {
            throw new ApiException("Not authorized to access this product");
        }
    }
}