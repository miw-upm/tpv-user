package es.upm.api.configurations;

import java.util.Arrays;
import java.util.List;

public enum Scope {
    OPENID, PROFILE, OFFLINE_ACCESS;

    public static final String PREFIX = "SCOPE_";

    public static List<String> allValues() {
        return Arrays.stream(Scope.values())
                .map(Scope::value)
                .toList();
    }

    public static Scope of(String withPrefix) {
        return Scope.valueOf(withPrefix
                .replace(PREFIX, "")
                .toUpperCase());
    }

    public String scopeValue() {
        return PREFIX + this.value();
    }

    public String value() {
        return this.name().toLowerCase();
    }

}
