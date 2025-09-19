package org.hackcelestial.sportsbridge.Api.Services;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpPhoneService {
    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final int MAX_PER_PHONE_10_MIN = 5;
    private static final int MAX_PER_IP_10_MIN = 20;

    private final Random random = new Random();

    private static class OtpEntry {
        String otp;
        Instant expiresAt;
        Map<String, String> payload; // signup metadata
    }

    private final Map<String, OtpEntry> byPhone = new ConcurrentHashMap<>();
    private final Map<String, Integer> phoneCounters = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipCounters = new ConcurrentHashMap<>();
    private Instant windowStart = Instant.now();

    private void rotateWindowIfNeeded() {
        if (Duration.between(windowStart, Instant.now()).toMinutes() >= 10) {
            phoneCounters.clear();
            ipCounters.clear();
            windowStart = Instant.now();
        }
    }

    public String startOtp(String phone, String ip, Map<String, String> payload) {
        rotateWindowIfNeeded();
        int pc = phoneCounters.getOrDefault(phone, 0);
        int ic = ipCounters.getOrDefault(ip, 0);
        if (pc >= MAX_PER_PHONE_10_MIN) throw new RuntimeException("Too many OTP requests for this phone. Try later.");
        if (ic >= MAX_PER_IP_10_MIN) throw new RuntimeException("Too many OTP requests from this IP. Try later.");

        String otp = String.format("%06d", random.nextInt(1_000_000));
        OtpEntry entry = new OtpEntry();
        entry.otp = otp;
        entry.expiresAt = Instant.now().plus(OTP_TTL);
        entry.payload = payload;
        byPhone.put(phone, entry);
        phoneCounters.put(phone, pc + 1);
        ipCounters.put(ip, ic + 1);

        // Stub: integrate with SMS provider here. For now just log to console.
        System.out.println("[OTP] Sending OTP " + otp + " to phone " + phone);
        return UUID.randomUUID().toString();
    }

    public Map<String, String> verifyOtp(String phone, String otp) {
        OtpEntry entry = byPhone.get(phone);
        if (entry == null) throw new RuntimeException("OTP not found. Re-initiate signup.");
        if (Instant.now().isAfter(entry.expiresAt)) {
            byPhone.remove(phone);
            throw new RuntimeException("OTP expired.");
        }
        if (!entry.otp.equals(otp)) throw new RuntimeException("Invalid OTP.");
        byPhone.remove(phone);
        return entry.payload;
    }

    // Dev helper: peek current OTP for a phone (returns null if absent/expired)
    public String peekOtp(String phone) {
        OtpEntry e = byPhone.get(phone);
        if (e == null) return null;
        if (Instant.now().isAfter(e.expiresAt)) {
            byPhone.remove(phone);
            return null;
        }
        return e.otp;
    }
}
