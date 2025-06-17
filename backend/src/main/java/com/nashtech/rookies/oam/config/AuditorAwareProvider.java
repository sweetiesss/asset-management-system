package com.nashtech.rookies.oam.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareProvider {
    private final AuditorAware<String> auditorAware;

    public AuditorAwareProvider(AuditorAware<String> auditorAware) {
        this.auditorAware = auditorAware;
    }

    public Optional<String> getCurrentAuditor() {
        return auditorAware.getCurrentAuditor();
    }

}
