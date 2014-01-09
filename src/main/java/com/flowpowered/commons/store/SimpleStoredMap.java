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
package com.flowpowered.commons.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Represents a map for mapping Strings to and Object. All conversions are cached in a store.
 */
public class SimpleStoredMap<T> implements StoredMap<T> {
	protected final SimpleStore<T> store;
	protected final String name;

	/**
	 * @param store the store to store ids
	 * @param name The name of this StringMap
	 */
	public SimpleStoredMap(SimpleStore<T> store, String name) {
		this.store = store;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Registers a key/id pair with the map.  If the id is already in use the method will fail.<br> <br> The id must be lower than the min id for the map to prevent clashing with the dynamically
	 * allocated ids
	 *
	 * @param key the key to be added
	 * @param value the desired value to be matched to the key
	 * @return true if the key/id pair was successfully registered
	 * @throws IllegalArgumentException if the id >= minId
	 */
	@Override
	public boolean register(String key, T value) {
		return store.setIfAbsent(key, value);
	}

	/**
	 * Gets the String corresponding to a given int.
	 *
	 * @return the String or null if no match
	 */
	@Override
	public String getString(T value) {
		return store.reverseGet(value);
	}

	/**
	 * Gets the int corresponding to a given String
	 *
	 * @param key The key
	 * @return The int or null if no match
	 */
	@Override
	public T getValue(String key) {
		return store.get(key);
	}

	/**
	 * Saves the map to the persistence system
	 *
	 * @return returns true if the map saves correctly
	 */
	@Override
	public boolean save() {
		return store.save();
	}

	/**
	 * Returns a collection of all keys for all (key, value) pairs within the Store
	 *
	 * @return returns a Collection containing all the keys
	 */
	@Override
	public Collection<String> getKeys() {
		return store.getKeys();
	}

	@Override
	public List<Pair<T, String>> getItems() {
		List<Pair<T, String>> items = new ArrayList<>();
		for (Map.Entry<String, T> entry : store.getEntrySet()) {
			items.add(new ImmutablePair<>(entry.getValue(), entry.getKey()));
		}
		return items;
	}

	@Override
	public void clear() {
		store.clear();
	}
}
