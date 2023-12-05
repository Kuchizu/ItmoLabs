package io.github.Lab3.model;

public class AreaResultChecker {
    public static boolean getResult(double x, double y, double r) {
        // Check 1st square -- Rectangle
        if (x >= 0 && y >= 0) {
            if (x <= r && y <= r / 2) {
                return true;
            }
        }

        // Check 2nd square -- Always false

        // Check 3rd square -- Triangle
        if (x <= 0 && y <= 0) {
            return -x - 2 * y <= r;
        }

        // Check 4th square -- 1/4 circle
        if (x >= 0 && y <= 0){
            return Math.pow(x, 2) + Math.pow(y, 2) <= Math.pow(r, 2);
        }

        return false;
    }
}
