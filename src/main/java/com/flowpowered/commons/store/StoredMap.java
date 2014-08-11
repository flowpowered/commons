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
package com.flowpowered.commons.store;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.flowpowered.commons.Named;

public interface StoredMap<T> extends Named {
    public void clear();

    public List<Pair<T, String>> getItems();

    /**
     * Returns a collection of all keys for all (key, value) pairs within the Store
     *
     * @return returns a Collection containing all the keys
     */
    public Collection<String> getKeys();

    public String getName();

    /**
     * Gets the String corresponding to a given int.
     *
     * @return the String or null if no match
     */
    public String getString(T value);

    /**
     * Gets the int corresponding to a given String
     *
     * @param key The key
     * @return The int or null if no match
     */
    public T getValue(String key);

    /**
     * Registers a key/id pair with the map.  If the id is already in use the method will fail.<br> <br> The id must be lower than the min id for the map to prevent clashing with the dynamically
     * allocated ids
     *
     * @param key the key to be added
     * @param value the desired value to be matched to the key
     * @return true if the key/id pair was successfully registered
     * @throws IllegalArgumentException if the id >= minId
     */
    public boolean register(String key, T value);

    /**
     * Saves the map to the persistence system
     *
     * @return returns true if the map saves correctly
     */
    public boolean save();
}
