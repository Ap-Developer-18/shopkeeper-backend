package com.shopkeeper.app.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioNotificationService implements NotificationService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.sms-from-number}")
    private String smsFromNumber;

    @Value("${twilio.whatsapp-from-number}")
    private String whatsappFromNumber;

    @PostConstruct
    public void init() {
        try {
            if (isConfigured()) {
                Twilio.init(accountSid, authToken);
            } else {
                log.warn("Running in STUB mode (No Twilio configured).");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Twilio: {}", e.getMessage());
        }
    }

    @Override
    public void sendSms(String toPhone, String body) {
        if (!isConfigured()) {
            System.out.println("[SMS-STUB] to=" + toPhone + " | " + body);
            return;
        }
        try {
            Message.creator(new PhoneNumber(toPhone), new PhoneNumber(smsFromNumber), body).create();
        } catch (Exception e) {
            log.error("Failed to send SMS: {}", e.getMessage());
        }
    }

    @Override
    public void sendWhatsApp(String toPhone, String body) {
        if (!isConfigured()) {
            System.out.println("[WHATSAPP-STUB] to=" + toPhone + " | " + body);
            return;
        }
        try {
            Message.creator(
                    new PhoneNumber("whatsapp:" + normalizeToE164(toPhone)),
                    new PhoneNumber(whatsappFromNumber),
                    body
            ).create();
        } catch (Exception e) {
            log.error("Failed to send WhatsApp: {}", e.getMessage());
        }
    }

    @Override
    public void notifyBoth(String toPhone, String body) {
        sendSms(toPhone, body);
        sendWhatsApp(toPhone, body);
    }

    @Override
    public void sendOtp(String toPhone, String otp, String purpose) {
        String body = "Your OTP is " + otp + ". Valid for 5 minutes.";
        sendSms(toPhone, body);
        sendWhatsApp(toPhone, body);
    }

    private boolean isConfigured() {
        return accountSid != null && !accountSid.startsWith("YOUR_");
    }

    private String normalizeToE164(String phone) {
        if (phone.startsWith("+")) return phone;
        return "+91" + phone;
    }
}