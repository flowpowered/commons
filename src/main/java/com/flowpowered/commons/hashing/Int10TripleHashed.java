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

public class Int10TripleHashed {
    private int bx;
    private int by;
    private int bz;

    public Int10TripleHashed() {
    }

    public Int10TripleHashed(int bx, int by, int bz) {
        this.bx = bx;
        this.by = by;
        this.bz = bz;
    }

    /**
     * Sets the base of the hash to the given values
     */
    public final void setBase(int bx, int by, int bz) {
        this.bx = bx;
        this.by = by;
        this.bz = bz;
    }

    /**
     * Packs given x, y, z coordinates.  The coords must represent a point within a 1024 sized cuboid with the base at the (bx, by, bz)
     *
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param z an <code>int</code> value
     *
     * @return the packed int
     */
    public final int key(int x, int y, int z) {
        return (((x - bx) & 0x3FF) << 22) | (((y - by) & 0x3FF) << 11) | ((z - bz) & 0x3FF);
    }

    /**
     * Gets the x coordinate value from the int key
     *
     * @param key to get from
     *
     * @return the x coord
     */
    public final int keyX(int key) {
        return bx + ((key >> 22) & 0x3FF);
    }

    /**
     * Gets the y coordinate value from the int key
     *
     * @param key to get from
     *
     * @return the y coord
     */
    public final int keyY(int key) {
        return by + ((key >> 11) & 0x3FF);
    }

    /**
     * Gets the y coordinate value from the int key
     *
     * @param key to get from
     *
     * @return the y coord
     */
    public final int keyZ(int key) {
        return bz + (key & 0x3FF);
    }
}
