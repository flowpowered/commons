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

public class NibblePairHashed {

    /**
     * Packs the first 4 most significant bits of each byte into a <code>byte</code>
     *
     * @param key1 a <code>byte</code> value
     * @param key2 a <code>byte</code> value
     *
     * @return The first 4 most significant bits of each byte packed into a <code>byte</code>
     */
    public static byte key(int key1, int key2) {
        return (byte) (((key1 & 0xF) << 4) | (key2 & 0xF));
    }

    /**
     * Packs the first 4 most significant bits of each byte into an <code>int</code> with the top 16 bits as zero.
     *
     * @param key1 a <code>byte</code> value
     * @param key2 a <code>byte</code> value
     *
     * @return The first 4 most significant bits of each byte packed into a <code>byte</code>
     */
    public static int intKey(int key1, int key2) {
        return key(key1, key2) & 0xFF;
    }

    /**
     * Sets 4 most significant bits in the composite to the 4 least significant bits in the key
     */
    public static byte setKey1(int composite, int key1) {
        return (byte) (((key1 & 0xF) << 4) | (composite & 0xF));
    }

    /**
     * Sets 4 least significant bits in the composite to the 4 least significant bits in the key
     */
    public static byte setKey2(int composite, int key2) {
        return (byte) ((composite & 0xF0) | (key2 & 0xF));
    }

    /**
     * Returns the 4 most significant bits in the byte value.
     *
     * @param composite to separate
     *
     * @return the 4 most significant bits in a byte
     */
    public static byte key1(int composite) {
        return (byte) ((composite >> 4) & 0xF);
    }

    /**
     * Returns the 4 least significant bits in the byte value.
     *
     * @param composite to separate
     *
     * @return the 4 least significant bits in a byte
     */
    public static byte key2(int composite) {
        return (byte) (composite & 0xF);
    }
}
