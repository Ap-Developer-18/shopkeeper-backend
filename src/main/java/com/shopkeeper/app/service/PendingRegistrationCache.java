package com.shopkeeper.app.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived, in-memory holder for the name/companyName captured at
 * registration Step 1, so Step 2 (which only receives mobileNumber + OTP,
 * per the flow spec) can attach them to the RegistrationToken once the
 * mobile number is verified. Entries older than 15 minutes are treated as
 * expired and ignored - a fresh Step 1 call is required in that case.
 */
@Component
public class PendingRegistrationCache {

    private record Entry(String name, String companyName, LocalDateTime capturedAt) {
    }

    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<>();

    public void put(String mobileNumber, String name, String companyName) {
        cache.put(mobileNumber, new Entry(name, companyName, LocalDateTime.now()));
    }

    public String[] getNameAndCompany(String mobileNumber) {
        Entry entry = cache.get(mobileNumber);
        if (entry == null || entry.capturedAt().isBefore(LocalDateTime.now().minusMinutes(15))) {
            return new String[]{null, null};
        }
        return new String[]{entry.name(), entry.companyName()};
    }

    public void evict(String mobileNumber) {
        cache.remove(mobileNumber);
    }
}
