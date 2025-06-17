package com.nashtech.rookies.oam.config;

import com.nashtech.rookies.oam.model.AuditableEntity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

public class CustomAuditingEntityListener {
    private AuditorAwareProvider getAuditorAwareProvider() {
        return SpringContext.getBean(AuditorAwareProvider.class);
    }

    @PrePersist
    public void onCreate(Object target) {
        if (target instanceof AuditableEntity entity) {
            var now = LocalDateTime.now();
            var auditor = getAuditorAwareProvider().getCurrentAuditor().orElse(null);

            entity.setCreatedAt(now);
            entity.setCreatedBy(auditor);
        }
    }

    @PreUpdate
    public void onUpdate(Object target) {
        if (target instanceof AuditableEntity entity) {
            var now = LocalDateTime.now();
            var auditor = getAuditorAwareProvider().getCurrentAuditor().orElse(null);

            entity.setUpdatedAt(now);
            entity.setUpdatedBy(auditor);
        }
    }
}
