package com.nashtech.rookies.oam.model;

import com.nashtech.rookies.oam.config.CustomAuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(CustomAuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AuditableEntity {
    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    String createdBy;

    @LastModifiedBy
    String updatedBy;
}