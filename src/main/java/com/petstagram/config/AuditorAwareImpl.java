package com.petstagram.config;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        String modifiedBy = "guest";
        return Optional.of(modifiedBy);
    }
}