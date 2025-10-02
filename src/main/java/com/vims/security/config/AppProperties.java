package com.vims.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@ConfigurationProperties(prefix = "security.password")
public class AppProperties {
    private int minLength = 8;
    private boolean requireUppercase = true;
    private boolean requireNumber = true;

    public int getMinLength() { return minLength; }
    public void setMinLength(int minLength) { this.minLength = minLength; }
    public boolean isRequireUppercase() { return requireUppercase; }
    public void setRequireUppercase(boolean requireUppercase) { this.requireUppercase = requireUppercase; }
    public boolean isRequireNumber() { return requireNumber; }
    public void setRequireNumber(boolean requireNumber) { this.requireNumber = requireNumber; }
}


