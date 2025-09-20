package org.hackcelestial.sportsbridge.Api.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Api.Entities.SbUser;
import org.hackcelestial.sportsbridge.Api.Repositories.SbUserRepository;
import org.hackcelestial.sportsbridge.Api.Services.CryptoService;
import org.hackcelestial.sportsbridge.Api.Services.JwtService;
import org.hackcelestial.sportsbridge.Api.Services.OtpPhoneService;
import org.hackcelestial.sportsbridge.Api.Security.CurrentUser;
import org.hackcelestial.sportsbridge.Enums.ApiUserRole;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Enums.UserRole;
import org.hackcelestial.sportsbridge.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

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
    // NEW: legacy repo for syncing into 'users' table
    @Autowired
    private UserRepository legacyUserRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final Pattern E164 = Pattern.compile("^\\+?\\d{10,15}$");

    private boolean isInvalidAadhaar(String a) {
        return a == null || !a.matches("^\\d{12}$");
    }
    private boolean isInvalidPhone(String p) {
        return p == null || !p.matches("^\\+?\\d{10,15}$");
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
        if (isInvalidAadhaar(body.aadhaar)) return ResponseEntity.badRequest().body(Map.of("error", "Invalid Aadhaar format"));
        if (isInvalidPhone(body.phone)) return ResponseEntity.badRequest().body(Map.of("error", "Invalid phone format"));
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
        if (isInvalidAadhaar(body.aadhaar)) return ResponseEntity.badRequest().body(Map.of("error", "Invalid Aadhaar format"));
        if (isInvalidPhone(body.phone)) return ResponseEntity.badRequest().body(Map.of("error", "Invalid phone format"));
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
        String fullName = Optional.ofNullable(payload.get("fullName")).orElse("User").trim();
        if (fullName.isEmpty()) fullName = "User";
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
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException dive) {
            // Idempotency/race: fetch existing by phone or aadhaar hash and update
            SbUser existing = userRepository.findByPhone(phone).orElseGet(() -> userRepository.findByAadhaarHash(ah).orElse(null));
            if (existing != null) {
                existing.setFullName(fullName);
                existing.setEmail(email);
                existing.setRole(ApiUserRole.valueOf(roleStr));
                existing.setAadhaarEncrypted(cryptoService.encryptAadhaar(aadhaar));
                existing.setAadhaarHash(ah);
                existing.setVerified(true);
                if (existing.getCreatedAt() == null) existing.setCreatedAt(LocalDateTime.now());
                user = userRepository.save(existing);
            } else {
                throw dive;
            }
        }

        // Sync into legacy 'users' table as well (best-effort, never fail main tx)
        try { syncLegacyUser(user); } catch (Exception ignore) { }

        // Ensure DB state is flushed before issuing token
        try { userRepository.flush(); } catch (Exception ignore) { }

        String token = jwtService.issueToken(user.getId(), user.getPhone());
        return ResponseEntity.ok(Map.of("token", token));
    }

    private void syncLegacyUser(SbUser apiUser) {
        String phone = apiUser.getPhone();
        String email = Optional.ofNullable(apiUser.getEmail()).orElse(phone + "@local");
        // Try find by phone first, fallback by email
        User legacy = legacyUserRepository.findByPhone(phone);
        if (legacy == null) legacy = legacyUserRepository.findByEmail(email);
        boolean isNew = (legacy == null);
        if (isNew) legacy = new User();

        // Split full name
        String full = Optional.ofNullable(apiUser.getFullName()).orElse("").trim();
        String first = full, last = "";
        int sp = full.indexOf(' ');
        if (sp > 0) { first = full.substring(0, sp); last = full.substring(sp+1); }

        legacy.setFirstName(first);
        legacy.setLastName(last);
        legacy.setEmail(email);
        legacy.setPhone(phone);
        legacy.setProfileImageUrl(apiUser.getProfilePicUrl());
        legacy.setActive(true);
        legacy.setRole(mapLegacyRole(apiUser.getRole()));
        if (legacy.getCreatedAt() == null) legacy.setCreatedAt(LocalDateTime.now());
        if (legacy.getPassword() == null || legacy.getPassword().isBlank()) {
            legacy.setPassword(encoder.encode(UUID.randomUUID().toString()));
        }
        try {
            legacyUserRepository.save(legacy);
            try { legacyUserRepository.flush(); } catch (Exception ignore) { }
        } catch (DataIntegrityViolationException dive) {
            // Likely unique email conflict: generate a unique local email and retry once
            legacy.setEmail(phone + "+" + UUID.randomUUID().toString().substring(0,8) + "@local");
            legacyUserRepository.save(legacy);
            try { legacyUserRepository.flush(); } catch (Exception ignore) { }
        }
    }

    private UserRole mapLegacyRole(ApiUserRole apiRole) {
        if (apiRole == null) return UserRole.ATHELETE;
        return switch (apiRole) {
            case ATHLETE -> UserRole.ATHELETE; // legacy enum typo
            case COACH -> UserRole.COACH;
            case SPONSOR -> UserRole.SPONSOR;
            default -> UserRole.ATHELETE;
        };
    }

    // NEW: Set password for the current user (requires JWT from verify/login)
    public static class SetPasswordRequest { public String password; }

    @PostMapping("/set-password")
    @Transactional
    public ResponseEntity<?> setPassword(@CurrentUser SbUser currentUser, @RequestBody SetPasswordRequest body) {
        if (currentUser == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        if (body == null || body.password == null || body.password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
        }
        currentUser.setPasswordHash(encoder.encode(body.password));
        userRepository.save(currentUser);
        return ResponseEntity.ok(Map.of("status", "PASSWORD_SET"));
    }

    // NEW: Password login (phone + password)
    public static class PasswordLoginRequest { public String phone; public String password; }

    @PostMapping("/password-login")
    public ResponseEntity<?> passwordLogin(@RequestBody PasswordLoginRequest body) {
        try {
            if (body == null || body.phone == null || body.password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing phone or password"));
            }
            String raw = body.phone.trim();
            if (!E164.matcher(raw).matches()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid phone format"));
            }
            Optional<SbUser> ou = userRepository.findByPhone(raw);
            // Heuristic: if 10 digits without +country code, also try +91 prefix
            if (ou.isEmpty() && raw.matches("^\\d{10}$")) {
                ou = userRepository.findByPhone("+91" + raw);
            }
            // Heuristic: if +91 + 10 digits and not found, try bare 10 digits
            if (ou.isEmpty() && raw.startsWith("+91") && raw.substring(3).matches("^\\d{10}$")) {
                ou = userRepository.findByPhone(raw.substring(3));
            }
            if (ou.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Account not found or not verified"));
            }
            SbUser u = ou.get();
            if (!u.isVerified()) {
                return ResponseEntity.status(403).body(Map.of("error", "Account not verified"));
            }
            if (u.getPasswordHash() == null || !encoder.matches(body.password, u.getPasswordHash())) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
            }
            String token = jwtService.issueToken(u.getId(), u.getPhone());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal error"));
        }
    }

    @PostMapping("/session-attach")
    public ResponseEntity<?> sessionAttach(@CurrentUser SbUser currentUser, HttpSession session) {
        if (currentUser == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        User legacy = new User();
        // Split full name into first/last best-effort
        String full = Optional.ofNullable(currentUser.getFullName()).orElse("").trim();
        String first = full, last = "";
        int sp = full.indexOf(' ');
        if (sp > 0) { first = full.substring(0, sp); last = full.substring(sp+1); }
        legacy.setFirstName(first);
        legacy.setLastName(last);
        legacy.setEmail(Optional.ofNullable(currentUser.getEmail()).orElse(currentUser.getPhone()+"@local"));
        legacy.setPhone(currentUser.getPhone());
        legacy.setProfileImageUrl(currentUser.getProfilePicUrl());
        legacy.setActive(true);
        // Map API role to legacy role (ATHLETE -> ATHELETE typo handling)
        UserRole lr;
        try {
            ApiUserRole apiRole = currentUser.getRole();
            if (apiRole == ApiUserRole.ATHLETE) {
                lr = UserRole.ATHELETE;
            } else if (apiRole == ApiUserRole.COACH) {
                lr = UserRole.COACH;
            } else if (apiRole == ApiUserRole.SPONSOR) {
                lr = UserRole.SPONSOR;
            } else {
                // USER or any future role maps to athlete by default
                lr = UserRole.ATHELETE;
            }
        } catch (Exception e) { lr = UserRole.ATHELETE; }
        legacy.setRole(lr);
        session.setAttribute("user", legacy);
        return ResponseEntity.ok(Map.of("status", "SESSION_ATTACHED"));
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
