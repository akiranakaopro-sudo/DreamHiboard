package com.coui.appcompat.math;

public class COUIMathUtils {
    public static int floorDiv(int x, int y) {
        int result = x / y;
        return ((x ^ y) >= 0 || y * result == x) ? result : result - 1;
    }

    public static int floorMod(int x, int y) {
        return x - (floorDiv(x, y) * y);
    }
}
