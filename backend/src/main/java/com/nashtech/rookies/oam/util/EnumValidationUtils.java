package com.nashtech.rookies.oam.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class EnumValidationUtils {
    public static boolean isValidEnum(String value, Class<? extends Enum<?>> enumClass) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        for (Enum<?> enumVal : enumClass.getEnumConstants()) {
            if (enumVal.name().equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }

    public static String[] getEnumNames(Class<? extends Enum<?>> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> ((Enum<?>) e).name())
                .toArray(String[]::new);
    }
}
