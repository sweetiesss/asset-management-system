package com.nashtech.rookies.oam.model.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum AssignmentStatusType {
    ACCEPTED("Accepted"),
    DECLINED("Declined"),
    WAITING_FOR_ACCEPTANCE("Waiting for acceptance"),
    COMPLETED("Completed");

    private final String dbName;

    AssignmentStatusType(String dbName) {
        this.dbName = dbName;
    }

    public static Optional<AssignmentStatusType> fromDbName(String dbName) {
        return Arrays.stream(values())
                .filter(type -> type.dbName.equalsIgnoreCase(dbName))
                .findFirst();
    }


}
