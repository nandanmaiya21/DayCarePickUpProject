package com.example.daycarepickupproject.Models;

/** Small helpers for showing names. */
public final class NameUtils {

    private NameUtils() {
    }

    /** "Ava Okafor" -> "AO", "Ava" -> "A", "" -> "?". */
    public static String initials(String name) {
        if (name == null) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
            if (sb.length() == 2) {
                break;
            }
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    /** "Ava Okafor" -> "Ava". */
    public static String firstName(String name) {
        if (name == null) {
            return "";
        }
        String trimmed = name.trim();
        int space = trimmed.indexOf(' ');
        return space < 0 ? trimmed : trimmed.substring(0, space);
    }
}
