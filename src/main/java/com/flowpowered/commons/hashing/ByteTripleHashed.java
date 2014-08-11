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

public class ByteTripleHashed {

    /**
     * Packs the first 8 most significant bits of each byte into an <code>int</code>
     *
     * @param x an <code>byte</code> value
     * @param y an <code>byte</code> value
     * @param z an <code>byte</code> value
     *
     * @return The first 8 most significant bits of each byte packed into an <code>int</code>
     */
    public static int key(int x, int y, int z) {
        return (x & 0xFF) << 16 | (z & 0xFF) << 8 | y & 0xFF;
    }

    /**
     * Gets the first 8-bit integer value from an int key
     *
     * @param key to get from
     *
     * @return the first 8-bit integer value in the key
     */
    public static byte key1(int key) {
        return (byte) (key >> 16 & 0xFF);
    }

    /**
     * Gets the second 8-bit integer value from an int key
     *
     * @param key to get from
     *
     * @return the second 8-bit integer value in the key
     */
    public static byte key2(int key) {
        return (byte) (key & 0xFF);
    }

    /**
     * Gets the third 8-bit integer value from an int key
     *
     * @param key to get from
     *
     * @return the third 8-bit integer value in the key
     */
    public static byte key3(int key) {
        return (byte) (key >> 8 & 0xFF);
    }
}
