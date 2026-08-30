package com.shopkeeper.app.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateBillRequest {
    @NotNull private Long customerId;
    @NotEmpty private List<BillItemRequest> items;
    // if true, sends SMS + WhatsApp notification with bill summary right after creation
    private boolean notifyCustomer = true;
}
