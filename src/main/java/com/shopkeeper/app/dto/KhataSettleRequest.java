package com.shopkeeper.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KhataSettleRequest {
    @NotNull private BigDecimal amount; // amount being settled now
}
