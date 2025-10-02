package com.vims.security.service;

import com.vims.security.config.AppProperties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordPolicyServiceTest {
    @Test
    void validates_policy_requirements() {
        AppProperties props = new AppProperties();
        props.setMinLength(8);
        props.setRequireUppercase(true);
        props.setRequireNumber(true);
        PasswordPolicyService svc = new PasswordPolicyService(props);

        assertDoesNotThrow(() -> svc.validatePassword("Password1"));
        assertThrows(IllegalArgumentException.class, () -> svc.validatePassword("pass1"));
        assertThrows(IllegalArgumentException.class, () -> svc.validatePassword("password1"));
        assertThrows(IllegalArgumentException.class, () -> svc.validatePassword("Password"));
    }
}


