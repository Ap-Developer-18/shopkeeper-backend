package com.shopkeeper.app.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Sends notifications via Twilio (SMS + WhatsApp).
 * Wrapped in try/catch so a notification failure never breaks the core
 * billing/auth flow. Falls back to a console "stub" log when Twilio
 * credentials aren't configured, so local development works out of the box.
 *
 * SECURITY: OTP values are never logged in plaintext at INFO level or above -
 * only a masked placeholder is logged, per the no-OTP-in-logs requirement.
 */
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
                log.warn("Twilio credentials not configured - SMS/WhatsApp notifications are disabled (stub mode).");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Twilio: {}", e.getMessage());
        }
    }

    @Override
    public void sendSms(String toPhone, String body) {
        try {
            if (!isConfigured()) {
                log.info("[SMS-STUB] to={}", toPhone);
                return;
            }
            Message.creator(new PhoneNumber(toPhone), new PhoneNumber(smsFromNumber), body).create();
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhone, e.getMessage());
        }
    }

    @Override
    public void sendWhatsApp(String toPhone, String body) {
        try {
            if (!isConfigured()) {
                log.info("[WHATSAPP-STUB] to={}", toPhone);
                return;
            }
            Message.creator(
                    new PhoneNumber("whatsapp:" + normalizeToE164(toPhone)),
                    new PhoneNumber(whatsappFromNumber),
                    body
            ).create();
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", toPhone, e.getMessage());
        }
    }

    @Override
    public void notifyBoth(String toPhone, String body) {
        sendSms(toPhone, body);
        sendWhatsApp(toPhone, body);
    }

    @Override
    public void sendOtp(String toPhone, String otp, String purpose) {
        String body = "Your OTP is " + otp + ". It is valid for 5 minutes. Do not share this with anyone.";
        // Do NOT log `otp` or `body` - only log that an OTP was dispatched.
        log.info("Dispatching OTP for purpose={} to a verified mobile number", purpose);
        sendSms(toPhone, body);
        sendWhatsApp(toPhone, body);
    }

    private boolean isConfigured() {
        return accountSid != null && !accountSid.startsWith("YOUR_");
    }

    private String normalizeToE164(String phone) {
        if (phone.startsWith("+")) return phone;
        return "+91" + phone; // default India country code
    }
}
