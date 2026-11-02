package com.example.sportsbook.common.util;

import java.util.regex.Pattern;

/** Masks common PII fragments before they reach audit logs or diagnostics. */
public final class PiiMasker {

    private static final Pattern EMAIL = Pattern.compile(
            "([A-Za-z0-9._%+-]{1,3})[A-Za-z0-9._%+-]*(@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})");
    private static final Pattern LONG_DIGITS = Pattern.compile("\\b(\\d{2})\\d{4,}(\\d{2})\\b");

    private PiiMasker() {
    }

    public static String mask(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String masked = EMAIL.matcher(input).replaceAll("$1***$2");
        return LONG_DIGITS.matcher(masked).replaceAll("$1****$2");
    }
}