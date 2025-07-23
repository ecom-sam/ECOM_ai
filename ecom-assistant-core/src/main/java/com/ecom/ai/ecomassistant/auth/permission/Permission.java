package com.ecom.ai.ecomassistant.auth.permission;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface Permission {

    String getCode();
    String getGroup();
    String getLabel();
    String getDescription();

    static <T extends Enum<T> & Permission> Optional<T> fromCode(Class<T> enumClass, String code) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(p -> p.getCode().equals(code))
                .findFirst();
    }

    static <T extends Enum<T> & Permission> List<T> fromGroup(Class<T> enumClass, String group) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(p -> p.getGroup().equalsIgnoreCase(group))
                .toList();
    }

    static <T extends Enum<T> & Permission> Optional<T> fromName(Class<T> enumClass, String name) {
        try {
            return Optional.of(Enum.valueOf(enumClass, name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
