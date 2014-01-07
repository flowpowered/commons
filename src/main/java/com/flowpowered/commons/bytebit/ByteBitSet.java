/*
 * This file is part of Spout LLC, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 ${organization} <http://www.spout.org/>
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
package com.flowpowered.commons.bytebit;

import com.flowpowered.commons.LogicUtil;

public class ByteBitSet {
	private byte flag;

	public ByteBitSet() {
		this.flag = 0;
	}

	public ByteBitSet(int flag) {
		this.flag = (byte) flag;
	}

	public ByteBitSet(ByteBitMask flag) {
		this.flag = flag.getMask();
	}

	public ByteBitSet(ByteBitMask... flags) {
		this.flag = 0;
		for (ByteBitMask flag : flags) {
			this.flag |= flag.getMask();
		}
	}

	/**
	 * Checks if a bit in this flag is set
	 */
	public boolean isDirty() {
		return this.flag != 0;
	}

	/**
	 * Gets the current flag as a byte
	 *
	 * @return the current flag
	 */
	public byte get() {
		return this.flag;
	}

	/**
	 * Gets the current state of one or more bits using a mask<br> If multiple bits are contained in the mask, all bits have to match
	 *
	 * @param mask to use
	 * @return True if all of the bits in the mask are set, False if not
	 */
	public boolean get(ByteBitMask mask) {
		return LogicUtil.getBit(this.flag, mask.getMask());
	}

	/**
	 * Gets the current state of one or more bits using a mask<br> If multiple bits are contained in the mask, one of these bits have to match
	 *
	 * @param mask containing the bit flags
	 * @return True if one of the bits in the mask are set, False if not
	 */
	public boolean getAny(ByteBitMask mask) {
		return (this.flag & mask.getMask()) != 0;
	}

	/**
	 * Sets the current flag
	 *
	 * @param flag to set to
	 */
	public void set(ByteBitMask mask) {
		this.flag = mask.getMask();
	}

	/**
	 * Sets the current flag
	 *
	 * @param flag to set to
	 */
	public void set(byte flag) {
		this.flag = flag;
	}

	/**
	 * Sets the current state of a bit using a mask
	 *
	 * @param mask to use
	 * @param value to set the bit to
	 */
	public void set(ByteBitMask mask, boolean value) {
		this.flag = LogicUtil.setBit(this.flag, mask.getMask(), value);
	}
}
