package com.shopkeeper.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotNull private Long billId;
    @NotNull private String mode; // "UPI" or "CASH"
    @NotNull private BigDecimal amountPaid;

    private String senderName;
    private String senderUpiId;
    private String senderPhone;

    private String receiverName;
    private String receiverUpiId;
    private String receiverPhone;

    private String transactionRef;

    // if amountPaid < bill total, the remainder is auto-added to udhaar khata
    private boolean addRemainderToKhata = true;
    private String khataDueDate; // ISO date (yyyy-MM-dd), optional, used when remainder goes to khata
}
