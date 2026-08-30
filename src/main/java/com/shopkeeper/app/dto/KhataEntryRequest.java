package com.shopkeeper.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KhataEntryRequest {
    @NotNull private Long customerId;
    @NotNull private String type; // YOU_GAVE or YOU_GOT
    @NotNull private BigDecimal amount;
    private String note;
    private String dueDate; // ISO date yyyy-MM-dd
}
