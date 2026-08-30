package com.shopkeeper.app.exception;

public class OTPAttemptExceededException extends RuntimeException {
    public OTPAttemptExceededException(String message) {
        super(message);
    }
}
