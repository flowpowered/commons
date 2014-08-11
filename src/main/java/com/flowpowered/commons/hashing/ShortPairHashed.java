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

public class ShortPairHashed {

    /**
     * Squashes 2 short values into 1 int, with the first value in the most significant bits and the second value in the least significant bits.
     *
     * @param key1 to squash
     * @param key2 to squash
     *
     * @return squashed int
     */
    public static int key(short key1, short key2) {
        return key1 << 16 | key2 & 0xFFFF;
    }

    /**
     * Returns the 16 most significant bits (short) in the int value.
     *
     * @param composite to separate
     *
     * @return the 16 most significant bits in an int
     */
    public static short key1(int composite) {
        return (short) ((composite >> 16) & 0xFFFF);
    }

    /**
     * Returns the 16 least significant bits (short) in the int value.
     *
     * @param composite to separate
     *
     * @return the 16 least significant bits in an int
     */
    public static short key2(int composite) {
        return (short) (composite & 0xFFFF);
    }
}
