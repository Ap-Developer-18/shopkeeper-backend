package com.shopkeeper.app.service;

import com.shopkeeper.app.dto.PaymentRequest;
import com.shopkeeper.app.entity.Bill;
import com.shopkeeper.app.entity.Payment;
import com.shopkeeper.app.exception.ApiException;
import com.shopkeeper.app.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillService billService;
    private final KhataService khataService;
    private final BarcodeService barcodeService;
    private final NotificationService notificationService;

    @Transactional
    public Payment recordPayment(PaymentRequest req) {
        Bill bill = billService.getById(req.getBillId());

        if (paymentRepository.findByBillId(bill.getId()).isPresent()) {
            throw new ApiException("Payment already recorded for this bill");
        }

        Payment.PaymentMode mode;
        try {
            mode = Payment.PaymentMode.valueOf(req.getMode().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException("Payment mode must be UPI or CASH");
        }

        // Generate barcode encoding bill + payment reference, for scan-to-pay / verify at counter
        String payload = "BILL:" + bill.getBillNumber() + ";AMT:" + req.getAmountPaid() + ";MODE:" + mode;
        String barcodePath = barcodeService.generateBarcode(payload, bill.getBillNumber());

        Payment payment = Payment.builder()
                .bill(bill)
                .mode(mode)
                .amountPaid(req.getAmountPaid())
                .senderName(req.getSenderName())
                .senderUpiId(req.getSenderUpiId())
                .senderPhone(req.getSenderPhone())
                .receiverName(req.getReceiverName() != null ? req.getReceiverName() : bill.getShopkeeper().getCompanyName())
                .receiverUpiId(req.getReceiverUpiId())
                .receiverPhone(req.getReceiverPhone() != null ? req.getReceiverPhone() : bill.getShopkeeper().getMobileNumber())
                .transactionRef(req.getTransactionRef())
                .barcodeImagePath(barcodePath)
                .build();

        Payment saved = paymentRepository.save(payment);

        BigDecimal remainder = bill.getTotalAmount().subtract(req.getAmountPaid());

        if (remainder.compareTo(BigDecimal.ZERO) <= 0) {
            bill.setStatus(Bill.BillStatus.PAID);
        } else if (req.isAddRemainderToKhata()) {
            bill.setStatus(Bill.BillStatus.PARTIALLY_PAID);
            LocalDate dueDate = req.getKhataDueDate() != null ? LocalDate.parse(req.getKhataDueDate()) : null;
            khataService.addFromBill(bill, remainder, dueDate);
        } else {
            bill.setStatus(Bill.BillStatus.PARTIALLY_PAID);
        }

        String message = "Payment received: Rs." + req.getAmountPaid() + " (" + mode + ") for bill "
                + bill.getBillNumber() + ". "
                + (remainder.compareTo(BigDecimal.ZERO) > 0
                    ? "Pending balance: Rs." + remainder
                    : "Bill fully paid. Thank you!");
        notificationService.notifyBoth(bill.getCustomer().getPhone(), message);

        return saved;
    }

    public Payment getByBillId(Long billId) {
        return paymentRepository.findByBillId(billId)
                .orElseThrow(() -> new ApiException("No payment found for this bill"));
    }
}
