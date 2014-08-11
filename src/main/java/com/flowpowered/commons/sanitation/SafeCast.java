/*
 * This file is part of Flow Commons, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <https://spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.commons.sanitation;

public class SafeCast {

    public static long toLong(Object o, long def) {
        if (!(o instanceof Long)) {
            return def;
        }

        return (Long) o;
    }

    public static int toInt(Object o, int def) {
        if (!(o instanceof Integer)) {
            return def;
        }

        return (Integer) o;
    }

    public static byte toByte(Object o, byte def) {
        if (!(o instanceof Byte)) {
            return def;
        }

        return (Byte) o;
    }

    public static float toFloat(Object o, float def) {
        if (!(o instanceof Float)) {
            return def;
        }

        return (Float) o;
    }

    public static byte[] toByteArray(Object o, byte[] def) {
        if (!(o instanceof byte[])) {
            return def;
        }

        return (byte[]) o;
    }

    public static short[] toShortArray(Object o, short[] def) {
        if (!(o instanceof short[])) {
            return def;
        }

        return (short[]) o;
    }

    public static int[] toIntArray(Object o, int[] def) {
        if (!(o instanceof int[])) {
            return def;
        }

        return (int[]) o;
    }

    public static String toString(Object o, String def) {
        if (!(o instanceof String)) {
            return def;
        }

        return (String) o;
    }

    public static <T, U extends T> T toGeneric(Object o, U def, Class<T> clazz) {
        if (o == null) {
            return def;
        }

        try {
            return clazz.cast(o);
        } catch (ClassCastException e) {
            return def;
        }
    }
}
