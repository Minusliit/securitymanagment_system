package com.vims.security.service;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

@Service
public class TotpService {
    private static final int TIME_STEP_SECONDS = 30;
    private static final int TOTP_DIGITS = 6;
    private static final String HMAC_ALGO = "HmacSHA1";

    public String generateSecret() {
        byte[] buffer = new byte[20];
        new SecureRandom().nextBytes(buffer);
        Base32 base32 = new Base32();
        String secret = base32.encodeToString(buffer);
        // Remove padding and ensure uppercase
        return secret.replaceAll("=", "").toUpperCase();
    }

    public boolean verifyCode(String base32Secret, int code) {
        long timeWindow = System.currentTimeMillis() / 1000L / TIME_STEP_SECONDS;
        for (int i = -1; i <= 1; i++) { // small clock drift tolerance
            int expected = generateTotp(base32Secret, timeWindow + i);
            if (expected == code) return true;
        }
        return false;
    }

    private int generateTotp(String base32Secret, long timeWindow) {
        try {
            byte[] key = new Base32().decode(base32Secret);
            byte[] data = new byte[8];
            long value = timeWindow;
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (value & 0xFF);
                value >>= 8;
            }
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(key, HMAC_ALGO));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
            int otp = binary % (int) Math.pow(10, TOTP_DIGITS);
            return otp;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate TOTP", e);
        }
    }
}


