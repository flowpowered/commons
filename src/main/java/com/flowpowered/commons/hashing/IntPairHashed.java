/*
 * This file is part of Flow Commons, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Flow Powered <https://flowpowered.com/>
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

public class IntPairHashed {
    /**
     * Creates a long key from 2 ints
     *
     * @param key1 an <code>int</code> value
     * @param key2 an <code>int</code> value
     *
     * @return a long which is the concatenation of key1 and key2
     */
    public static long key(int key1, int key2) {
        return (long) key1 << 32 | key2 & 0xFFFFFFFFL;
    }

    /**
     * Gets the first 32-bit integer value from an long key
     *
     * @param key to get from
     *
     * @return the first 32-bit integer value in the key
     */
    public static int key1(long key) {
        return (int) (key >> 32 & 0xFFFFFFFFL);
    }

    /**
     * Gets the second 32-bit integer value from an long key
     *
     * @param key to get from
     *
     * @return the second 32-bit integer value in the key
     */
    public static int key2(long key) {
        return (int) (key & 0xFFFFFFFFL);
    }
}
