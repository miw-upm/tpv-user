package es.upm.api.data.entities;

import java.util.Arrays;
import java.util.List;

public enum Role {
    ADMIN, MANAGER, OPERATOR, CUSTOMER, URL_TOKEN, ANONYMOUS, AUTHENTICATED;

    public static final String PREFIX = "ROLE_";

    public static List<String> allValues() {
        return Arrays.stream(Role.values())
                .map(Role::value)
                .toList();
    }

    public static Role of(String withPrefix) {
        return Role.valueOf(withPrefix
                .replace(PREFIX, "")
                .toUpperCase());
    }

    public String roleValue() {
        return PREFIX + this.value();
    }

    public String value() {
        return this.name().toLowerCase();
    }

}
