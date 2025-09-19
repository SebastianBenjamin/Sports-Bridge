package org.hackcelestial.sportsbridge.Api.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptoService {

    private final SecretKey aesKey;
    private final String pepper;
    private static final SecureRandom RANDOM = new SecureRandom();

    public CryptoService(@Value("${security.aesKeyBase64:testtesttesttesttesttesttesttest==}") String aesKeyBase64,
                         @Value("${security.aadhaarPepper:DEV_PEPPER}") String pepper) {
        this.pepper = pepper != null ? pepper : "DEV_PEPPER";
        SecretKey tmpKey;
        try {
            byte[] material;
            try {
                // Try Base64 first
                material = Base64.getDecoder().decode(aesKeyBase64);
            } catch (IllegalArgumentException ex) {
                // Fallback to raw UTF-8 bytes
                material = (aesKeyBase64 == null ? new byte[0] : aesKeyBase64.getBytes(StandardCharsets.UTF_8));
            }
            byte[] normalized = normalizeAesKey(material);
            tmpKey = new SecretKeySpec(normalized, "AES");
        } catch (Exception any) {
            // Never fail app startup; fallback to deterministic dev key
            byte[] fallback = normalizeAesKey("SportsBridgeDevKey16!!".getBytes(StandardCharsets.UTF_8));
            tmpKey = new SecretKeySpec(fallback, "AES");
        }
        this.aesKey = tmpKey;
    }

    // Ensure key length is one of 16, 24, 32 by padding/truncating deterministically
    private static byte[] normalizeAesKey(byte[] in) {
        if (in == null) in = new byte[0];
        int len = in.length;
        if (len == 16 || len == 24 || len == 32) return in;
        if (len == 0) {
            // produce a stable 16-byte key from an empty input
            return "SportsBridgeDevKey".substring(0, 16).getBytes(StandardCharsets.UTF_8);
        }
        if (len < 16) {
            byte[] out = new byte[16];
            System.arraycopy(in, 0, out, 0, len);
            return out;
        }
        if (len < 24) {
            byte[] out = new byte[24];
            System.arraycopy(in, 0, out, 0, Math.min(len, 24));
            return out;
        }
        if (len < 32) {
            byte[] out = new byte[32];
            System.arraycopy(in, 0, out, 0, Math.min(len, 32));
            return out;
        }
        // Too long: truncate to 32
        byte[] out = new byte[32];
        System.arraycopy(in, 0, out, 0, 32);
        return out;
    }

    public byte[] encryptAadhaar(String aadhaarPlain) {
        try {
            byte[] iv = new byte[12];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
            byte[] ct = cipher.doFinal(aadhaarPlain.getBytes(StandardCharsets.UTF_8));
            // store as iv || ct (ct includes GCM tag)
            ByteBuffer bb = ByteBuffer.allocate(12 + ct.length);
            bb.put(iv);
            bb.put(ct);
            return bb.array();
        } catch (Exception e) {
            // Guard rails: never propagate to break runtime flows
            return new byte[0];
        }
    }

    public String aadhaarHash(String aadhaarPlain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(pepper.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(aadhaarPlain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return ""; // do not crash
        }
    }
}
