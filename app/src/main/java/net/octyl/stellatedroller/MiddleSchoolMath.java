/*
 * Copyright (c) Gradle, Inc.
 * Copyright (c) Octavia Togami <https://octyl.net>
 *
 * All Rights Reserved.
 */

package net.octyl.stellatedroller;

import org.spongepowered.math.vector.Vector2d;

public class MiddleSchoolMath {
    public static int gcd(int a, int b) {
        if (a == 0) {
            return Math.abs(b);
        }
        if (b == 0) {
            return Math.abs(a);
        }
        while (a != b) {
            if (a > b) {
                a -= b;
            } else {
                b -= a;
            }
        }
        return a;
    }

    public static Vector2d createFlippedDirectionRad(double rad) {
        return Vector2d.createDirectionRad(rad).mul(1, -1);
    }
}
