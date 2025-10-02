package com.vims.security.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TotpServiceTest {
    @Test
    void generates_secret_and_rejects_wrong_code() {
        TotpService svc = new TotpService();
        String secret = svc.generateSecret();
        assertNotNull(secret);
        assertFalse(svc.verifyCode(secret, 123456));
    }
}


