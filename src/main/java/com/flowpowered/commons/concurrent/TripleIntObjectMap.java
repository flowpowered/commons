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
package com.flowpowered.commons.concurrent;

import java.util.Collection;

public interface TripleIntObjectMap<T> {

	/**
	 * Gets the value for the given (x, y, z) key, or null if none
	 *
	 * @return the value
	 */
	public T get(int x, int y, int z);

	/**
	 * Removes the key/value pair for the given (x, y, z) key
	 *
	 * @return the value removed, or null on failure
	 */
	public T remove(int x, int y, int z);

	/**
	 * Removes the given key/value pair
	 *
	 * @return true if the key/value pair was removed
	 */
	public boolean remove(int x, int y, int z, T value);

	/**
	 * Adds the given key/value pair to the map
	 *
	 * @param value the non-null value
	 *
	 * @return the old value
	 */
	public T put(int x, int y, int z, T value);

	/**
	 * Adds the given key/value pair to the map, but only if the key does not already map to a value
	 *
	 * @param value the non-null value
	 *
	 * @return the current value, or null on success
	 */
	public T putIfAbsent(int x, int y, int z, T value);

	/**
	 * Returns a collection containing all the values in the Map
	 */
	public Collection<T> valueCollection();
}
