package com.shopkeeper.app.service;

import com.shopkeeper.app.dto.CustomerRequest;
import com.shopkeeper.app.entity.Customer;
import com.shopkeeper.app.entity.User;
import com.shopkeeper.app.exception.ApiException;
import com.shopkeeper.app.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CurrentUserService currentUserService;

    public Customer create(CustomerRequest req) {
        User shopkeeper = currentUserService.getCurrentUser();
        Customer customer = Customer.builder()
                .name(req.getName())
                .phone(req.getPhone())
                .whatsappNumber(req.getWhatsappNumber() != null ? req.getWhatsappNumber() : req.getPhone())
                .email(req.getEmail())
                .address(req.getAddress())
                .shopkeeper(shopkeeper)
                .build();
        return customerRepository.save(customer);
    }

    public List<Customer> listAll() {
        User shopkeeper = currentUserService.getCurrentUser();
        return customerRepository.findByShopkeeperId(shopkeeper.getId());
    }

    public List<Customer> search(String name) {
        User shopkeeper = currentUserService.getCurrentUser();
        return customerRepository.findByShopkeeperIdAndNameContainingIgnoreCase(shopkeeper.getId(), name);
    }

    public Customer getById(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ApiException("Customer not found"));
        ensureOwnership(c);
        return c;
    }

    public Customer update(Long id, CustomerRequest req) {
        Customer c = getById(id);
        c.setName(req.getName());
        c.setPhone(req.getPhone());
        c.setWhatsappNumber(req.getWhatsappNumber() != null ? req.getWhatsappNumber() : req.getPhone());
        c.setEmail(req.getEmail());
        c.setAddress(req.getAddress());
        return customerRepository.save(c);
    }

    public void delete(Long id) {
        Customer c = getById(id);
        customerRepository.delete(c);
    }

    private void ensureOwnership(Customer c) {
        User current = currentUserService.getCurrentUser();
        if (!c.getShopkeeper().getId().equals(current.getId())) {
            throw new ApiException("Not authorized to access this customer");
        }
    }
}
