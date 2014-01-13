/*
 * This file is part of Flow Commons, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
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
package com.flowpowered.commons.bit;

import com.flowpowered.commons.LogicUtil;

public class ShortBitSet implements ShortBitMask {
    private short flag;

    public ShortBitSet() {
        this.flag = 0;
    }

    public ShortBitSet(int flag) {
        this.flag = (short) flag;
    }

    public ShortBitSet(ShortBitMask flag) {
        this.flag = flag.getMask();
    }

    public ShortBitSet(ShortBitMask... flags) {
        this.flag = 0;
        for (ShortBitMask flag : flags) {
            this.flag |= flag.getMask();
        }
    }

    /**
     * Gets the current flag as a byte
     *
     * @return the current flag
     */
    public short get() {
        return this.flag;
    }

    /**
     * Gets the current state of one or more bits using a mask<br> If multiple bits are contained in the mask, all bits have to match
     *
     * @param mask to use
     * @return True if all of the bits in the mask are set, False if not
     */
    public boolean isEqual(ShortBitMask mask) {
        return LogicUtil.getBit(this.flag, mask.getMask());
    }

    /**
     * Gets the current state of one or more bits using a mask<br> If multiple bits are contained in the mask, one of these bits have to match
     *
     * @param mask containing the bit flags
     * @return True if one of the bits in the mask are set, False if not
     */
    public boolean isAny(ShortBitMask mask) {
        return (this.flag & mask.getMask()) != 0;
    }

    /**
     * Sets the current flag
     *
     * @param mask to set to
     */
    public void set(ShortBitMask mask) {
        this.flag = mask.getMask();
    }

    /**
     * Sets the current flag
     *
     * @param flag to set to
     */
    public void set(short flag) {
        this.flag = flag;
    }

    /**
     * Sets the current state of a bit using a mask
     *
     * @param mask to use
     * @param value to set the bit to
     */
    public void set(ShortBitMask mask, boolean value) {
        this.flag = LogicUtil.setBit(this.flag, mask.getMask(), value);
    }

    @Override
    public short getMask() {
        return flag;
    }

}
