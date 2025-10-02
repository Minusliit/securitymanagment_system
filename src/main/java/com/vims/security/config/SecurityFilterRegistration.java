package com.vims.security.config;

import com.vims.security.security.TwoFactorFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityFilterRegistration {
    @Bean
    public FilterRegistrationBean<TwoFactorFilter> twoFactorFilterRegistration(TwoFactorFilter filter) {
        FilterRegistrationBean<TwoFactorFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(1);
        return registration;
    }
}


