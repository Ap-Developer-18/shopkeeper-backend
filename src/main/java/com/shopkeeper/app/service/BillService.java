package com.shopkeeper.app.service;

import com.shopkeeper.app.dto.BillItemRequest;
import com.shopkeeper.app.dto.CreateBillRequest;
import com.shopkeeper.app.entity.*;
import com.shopkeeper.app.exception.ApiException;
import com.shopkeeper.app.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final CustomerService customerService;
    private final StockService stockService;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    @Transactional
    public Bill createBill(CreateBillRequest req) {
        User shopkeeper = currentUserService.getCurrentUser();
        Customer customer = customerService.getById(req.getCustomerId());

        Bill bill = Bill.builder()
                .billNumber(generateBillNumber(shopkeeper.getId()))
                .customer(customer)
                .shopkeeper(shopkeeper)
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<BillItem> billItems = new ArrayList<>();

        for (BillItemRequest itemReq : req.getItems()) {
            Product product = stockService.getById(itemReq.getProductId());

            // ===== item * number = total (core billing calculation) =====
            BigDecimal lineTotal = product.getPricePerUnit()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            BillItem billItem = BillItem.builder()
                    .bill(bill)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .pricePerUnit(product.getPricePerUnit())
                    .lineTotal(lineTotal)
                    .build();

            billItems.add(billItem);
            total = total.add(lineTotal);

            // deduct sold quantity from stock
            stockService.adjustStock(product.getId(), -itemReq.getQuantity());
        }

        bill.setItems(billItems);
        bill.setTotalAmount(total);
        Bill saved = billRepository.save(bill);

        if (req.isNotifyCustomer()) {
            String message = buildBillMessage(saved);
            notificationService.notifyBoth(customer.getPhone(), message);
        }

        return saved;
    }

    public Bill getById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ApiException("Bill not found"));
        ensureOwnership(bill);
        return bill;
    }

    public List<Bill> listAll() {
        User shopkeeper = currentUserService.getCurrentUser();
        return billRepository.findByShopkeeperId(shopkeeper.getId());
    }

    public List<Bill> listByCustomer(Long customerId) {
        return billRepository.findByCustomerId(customerId);
    }

    private String generateBillNumber(Long shopkeeperId) {
        long count = billRepository.countByShopkeeperId(shopkeeperId) + 1;
        return "INV-" + shopkeeperId + "-" + String.format("%06d", count);
    }

    private String buildBillMessage(Bill bill) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bill ").append(bill.getBillNumber())
          .append(" from ").append(bill.getShopkeeper().getCompanyName()).append("\n");
        for (BillItem item : bill.getItems()) {
            sb.append("- ").append(item.getProduct().getName())
              .append(" x").append(item.getQuantity())
              .append(" = Rs.").append(item.getLineTotal()).append("\n");
        }
        sb.append("Total: Rs.").append(bill.getTotalAmount());
        return sb.toString();
    }

    private void ensureOwnership(Bill bill) {
        User current = currentUserService.getCurrentUser();
        if (!bill.getShopkeeper().getId().equals(current.getId())) {
            throw new ApiException("Not authorized to access this bill");
        }
    }
}
