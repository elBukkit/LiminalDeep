package com.elmakers.mine.bukkit.plugins.liminal.random;

import java.util.Random;

public class RandomUtils {
    public static double range(Random random, double min, double max) {
        if (min >= max) {
            return min;
        }
        return min + random.nextDouble(max - min);
    }

    public static int range(Random random, int min, int max) {
        if (min >= max) {
            return min;
        }
        return min + random.nextInt(max - min);
    }

    public static double lerp(int value, int min, int max) {
        return Math.min(1.0, Math.max(0.0, (double)value / (double)(max - min)));
    }
}
