package com.nashtech.rookies.oam.service.enums;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.exception.AssignmentNotDeletableException;
import com.nashtech.rookies.oam.exception.AssignmentNotUpdatableException;
import lombok.Getter;

import java.util.function.Supplier;

@Getter
public enum AssignmentActionRule {
    UPDATE("updatable", () -> new AssignmentNotUpdatableException(ErrorCode.ASSIGNMENT_NOT_UPDATABLE.getMessage())),
    DELETE("deletable", () -> new AssignmentNotDeletableException(ErrorCode.ASSIGNMENT_NOT_DELETABLE.getMessage()));

    private final String action;
    private final Supplier<? extends RuntimeException> exceptionSupplier;

    AssignmentActionRule(String action, Supplier<? extends RuntimeException> exceptionSupplier) {
        this.action = action;
        this.exceptionSupplier = exceptionSupplier;
    }

    public RuntimeException getException() {
        return exceptionSupplier.get();
    }
}
