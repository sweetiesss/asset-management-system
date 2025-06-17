package com.nashtech.rookies.oam.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public enum AssetState {
    AVAILABLE,
    NOT_AVAILABLE,
    ASSIGNED,
    WAITING_FOR_RECYCLING,
    RECYCLED;

    public static List<AssetState> getAllStates() {
        return Arrays.asList(values());
    }

    public static Set<AssetState> getAllStatesSet() {
        return Set.of(values());
    }

    public static AssetState[] getAllStatesArray() {
        return values();
    }

    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    public boolean isAssigned() {
        return this == ASSIGNED;
    }

    public boolean isRecyclable() {
        return this == WAITING_FOR_RECYCLING || this == RECYCLED;
    }


}
