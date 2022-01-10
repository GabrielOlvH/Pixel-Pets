package me.steven.pixelpets.utils;

public class Utils {
    public static String toRomanNumeral(int level) {
        return switch (level) {
            case 0 -> "I";
            case 1 -> "II";
            case 2 -> "III";
            case 3 -> "IV";
            default -> Integer.toString(level);
        };
    }
}
