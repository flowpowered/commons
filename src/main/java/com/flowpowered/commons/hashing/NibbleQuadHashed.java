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
package com.flowpowered.commons.hashing;

public abstract class NibbleQuadHashed {

    /**
     * Packs the first 4 least significant bits of each byte into a <code>short</code>
     *
     * @param key1 a <code>byte</code> value
     * @param key2 a <code>byte</code> value
     * @param key3 a <code>byte</code> value
     * @param key4 a <code>byte</code> value
     *
     * @return The first 4 most significant bits of each byte packed into a <code>short</code>
     */
    public static short key(int key1, int key2, int key3, int key4) {
        return (short) ((key1 & 0xF) << 12 | (key2 & 0xF) << 8 | (key3 & 0xF) << 4 | key4 & 0xF);
    }

    /**
     * Gets the first 4-bit integer value from a short key
     *
     * @param key to get from
     *
     * @return the first 4-bit integer value in the key
     */
    public static byte key1(int key) {
        return (byte) ((key >> 12) & 0xF);
    }

    /**
     * Gets the second 4-bit integer value from a short key
     *
     * @param key to get from
     *
     * @return the second 4-bit integer value in the key
     */
    public static byte key2(int key) {
        return (byte) ((key >> 8) & 0xF);
    }

    /**
     * Gets the third 4-bit integer value from a short key
     *
     * @param key to get from
     *
     * @return the third 4-bit integer value in the key
     */
    public static byte key3(int key) {
        return (byte) ((key >> 4) & 0xF);
    }

    /**
     * Gets the fourth 4-bit integer value from a short key
     *
     * @param key to get from
     *
     * @return the fourth 4-bit integer value in the key
     */
    public static byte key4(int key) {
        return (byte) (key & 0xF);
    }
}
