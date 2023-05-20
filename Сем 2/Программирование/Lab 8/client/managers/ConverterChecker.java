package managers;

import collections.Furnish;

public class ConverterChecker {
    public static boolean isFloat(String input) {
        try {
            Float.parseFloat(input);
            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String input) {
        try {
            Double.parseDouble(input);
            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isLong(String input) {
        try {
            Long.parseLong(input);
            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isFurnish(String input) {
        try {
            Furnish.valueOf(input);
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}