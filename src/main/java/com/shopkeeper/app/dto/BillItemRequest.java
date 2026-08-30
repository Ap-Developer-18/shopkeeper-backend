package com.shopkeeper.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BillItemRequest {
    @NotNull private Long productId;
    @NotNull @Positive private Integer quantity; // item * number = total is computed server-side
}
