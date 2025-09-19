package org.hackcelestial.sportsbridge.Api.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.hackcelestial.sportsbridge.Api.Entities.SbUser;
import org.hackcelestial.sportsbridge.Api.Repositories.SbUserRepository;
import org.hackcelestial.sportsbridge.Api.Services.CryptoService;
import org.hackcelestial.sportsbridge.Api.Services.JwtService;
import org.hackcelestial.sportsbridge.Api.Services.OtpPhoneService;
import org.hackcelestial.sportsbridge.Enums.ApiUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private SbUserRepository userRepository;
    @Autowired
    private CryptoService cryptoService;
    @Autowired
    private OtpPhoneService otpPhoneService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private Environment environment;

    private boolean validAadhaar(String a) {
        return a != null && a.matches("^\\d{12}$");
    }
    private boolean validPhone(String p) {
        return p != null && p.matches("^\\+?\\d{10,15}$");
    }

    public static class SignupRequest {
        public String fullName;
        public String role; // ATHLETE/COACH/SPONSOR/USER
        public String phone;
        public String aadhaar;
        public String email; // optional
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest body, HttpServletRequest req) {
        if (!validAadhaar(body.aadhaar)) return ResponseEntity.badRequest().body(Map.of("error", "Invalid Aadhaar format"));
        if (!validPhone(body.phone)) return ResponseEntity.badRequest().body(Map.of("error", "Invalid phone format"));
        ApiUserRole role;
        try { role = ApiUserRole.valueOf(body.role == null ? "USER" : body.role); } catch (Exception e) { role = ApiUserRole.USER; }

        String aadhaarHash = cryptoService.aadhaarHash(body.aadhaar);
        Optional<SbUser> byHash = userRepository.findByAadhaarHash(aadhaarHash);
        Optional<SbUser> byPhone = userRepository.findByPhone(body.phone);

        if (byHash.isPresent() && !byHash.get().getPhone().equals(body.phone)) {
            return ResponseEntity.status(409).body(Map.of("error", "Aadhaar already registered with a different phone"));
        }
        if (byPhone.isPresent() && !byPhone.get().getAadhaarHash().equals(aadhaarHash)) {
            return ResponseEntity.status(409).body(Map.of("error", "Phone already registered with a different Aadhaar"));
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("type", "signup");
        payload.put("fullName", body.fullName);
        payload.put("role", role.name());
        payload.put("phone", body.phone);
        payload.put("aadhaar", body.aadhaar);
        if (body.email != null) payload.put("email", body.email);

        String ip = Optional.ofNullable(req.getHeader("X-Forwarded-For")).orElse(req.getRemoteAddr());
        String tx = otpPhoneService.startOtp(body.phone, ip, payload);
        return ResponseEntity.ok(Map.of("status", "OTP_SENT", "txId", tx));
    }

    public static class LoginRequest {
        public String phone;
        public String aadhaar;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body, HttpServletRequest req) {
        if (!validAadhaar(body.aadhaar)) return ResponseEntity.badRequest().body(Map.of("error", "Invalid Aadhaar format"));
        if (!validPhone(body.phone)) return ResponseEntity.badRequest().body(Map.of("error", "Invalid phone format"));
        String ah = cryptoService.aadhaarHash(body.aadhaar);
        Optional<SbUser> byPhone = userRepository.findByPhone(body.phone);
        if (byPhone.isEmpty() || !byPhone.get().getAadhaarHash().equals(ah)) {
            return ResponseEntity.status(404).body(Map.of("error", "Account not found for this Aadhaar + phone"));
        }
        if (!byPhone.get().isVerified()) {
            return ResponseEntity.status(403).body(Map.of("error", "Account not verified"));
        }
        Map<String, String> payload = new HashMap<>();
        payload.put("type", "login");
        payload.put("userId", String.valueOf(byPhone.get().getId()));
        String ip = Optional.ofNullable(req.getHeader("X-Forwarded-For")).orElse(req.getRemoteAddr());
        String tx = otpPhoneService.startOtp(body.phone, ip, payload);
        return ResponseEntity.ok(Map.of("status", "OTP_SENT", "txId", tx));
    }

    public static class VerifyRequest {
        public String phone;
        public String otp;
    }

    @PostMapping("/verify")
    @Transactional
    public ResponseEntity<?> verify(@RequestBody VerifyRequest body) {
        Map<String, String> payload = otpPhoneService.verifyOtp(body.phone, body.otp);
        String type = payload.getOrDefault("type", "signup");
        if ("login".equals(type)) {
            Long userId = Long.parseLong(payload.get("userId"));
            SbUser user = userRepository.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            String token = jwtService.issueToken(user.getId(), user.getPhone());
            return ResponseEntity.ok(Map.of("token", token));
        }
        // signup flow
        String fullName = payload.get("fullName");
        String roleStr = payload.getOrDefault("role", "USER");
        String phone = payload.get("phone");
        String aadhaar = payload.get("aadhaar");
        String email = payload.get("email");
        String ah = cryptoService.aadhaarHash(aadhaar);

        Optional<SbUser> byHash = userRepository.findByAadhaarHash(ah);
        Optional<SbUser> byPhone = userRepository.findByPhone(phone);
        if (byHash.isPresent() && !byHash.get().getPhone().equals(phone)) {
            return ResponseEntity.status(409).body(Map.of("error", "Aadhaar already used with another phone"));
        }
        if (byPhone.isPresent() && !byPhone.get().getAadhaarHash().equals(ah)) {
            return ResponseEntity.status(409).body(Map.of("error", "Phone already used with another Aadhaar"));
        }

        SbUser user = byPhone.orElseGet(SbUser::new);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(ApiUserRole.valueOf(roleStr));
        user.setAadhaarEncrypted(cryptoService.encryptAadhaar(aadhaar));
        user.setAadhaarHash(ah);
        user.setVerified(true);
        if (user.getCreatedAt() == null) user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        String token = jwtService.issueToken(user.getId(), user.getPhone());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/dev/peek-otp")
    public ResponseEntity<?> peekOtp(@RequestParam String phone) {
        // Only allow in dev profile
        boolean devActive = Arrays.asList(environment.getActiveProfiles()).contains("dev");
        if (!devActive) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
        String otp = otpPhoneService.peekOtp(phone);
        return ResponseEntity.ok(Map.of("otp", otp));
    }
}
