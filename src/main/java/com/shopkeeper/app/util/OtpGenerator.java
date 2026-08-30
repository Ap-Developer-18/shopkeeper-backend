package com.shopkeeper.app.util;

import java.security.SecureRandom;

public final class OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private OtpGenerator() {
    }

    /** Generates a cryptographically secure random 6-digit OTP (000000-999999, zero-padded). */
    public static String generate6Digit() {
        int number = RANDOM.nextInt(1_000_000);
        return String.format("%06d", number);
    }
}
