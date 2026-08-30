package com.shopkeeper.app.service;

/**
 * Abstraction over outbound customer/shopkeeper notifications (SMS, WhatsApp).
 * Keeping this as an interface lets the OTP/registration flow, billing, and
 * khata reminders all depend on the contract rather than a specific provider
 * (Twilio today, could be MSG91/Gupshup/etc. tomorrow).
 */
public interface NotificationService {

    void sendSms(String toPhone, String body);

    void sendWhatsApp(String toPhone, String body);

    /** Sends the same message via both channels - used for bill created / payment received events. */
    void notifyBoth(String toPhone, String body);

    /** Sends a one-time-password. Never logs the OTP itself in plaintext at INFO level or above. */
    void sendOtp(String toPhone, String otp, String purpose);
}
