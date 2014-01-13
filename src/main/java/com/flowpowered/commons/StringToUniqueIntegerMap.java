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
package com.flowpowered.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.flowpowered.commons.store.MemoryStore;
import com.flowpowered.commons.store.SimpleStore;
import com.flowpowered.commons.store.SimpleStoredMap;

/**
 * Represents a map for mapping Strings to unique ids.
 *
 * The class supports conversion of ids between maps and allocation of new unique ids for unknown Strings
 *
 * Conversions to and from parent/child maps are cached
 */
public class StringToUniqueIntegerMap extends SimpleStoredMap<Integer> {
    private final StringToUniqueIntegerMap parent;
    private final AtomicReferenceArray<Integer> thisToParentMap;
    private final AtomicReferenceArray<Integer> parentToThisMap;
    private final int minId;
    private final int maxId;
    private AtomicInteger nextId;

    public StringToUniqueIntegerMap(String name) {
        this(null, new MemoryStore<Integer>(), 0, Integer.MAX_VALUE, name);
    }

    public StringToUniqueIntegerMap(String name, int maxId) {
        this(null, new MemoryStore<Integer>(), 0, maxId, name);
    }

    /**
     * @param parent the parent of this map
     * @param store the store to store ids
     * @param minId the lowest valid id for dynamic allocation (ids below this are assumed to be reserved)
     * @param maxId the highest valid id + 1
     * @param name The name of this StringToUniqueIntegerMap
     */
    public StringToUniqueIntegerMap(StringToUniqueIntegerMap parent, SimpleStore<Integer> store, int minId, int maxId, String name) {
        super(store, name);
        this.parent = parent;
        if (this.parent != null) {
            thisToParentMap = new AtomicReferenceArray<>(maxId);
            parentToThisMap = new AtomicReferenceArray<>(maxId);
        } else {
            thisToParentMap = null;
            parentToThisMap = null;
        }
        this.minId = minId;
        this.maxId = maxId;
        nextId = new AtomicInteger(minId);
    }

    /**
     * Converts an id local to this map to the id local to the parent map
     *
     * @param localId to convert
     * @return the foreign id, or 0 on failure
     */
    public int convertToParent(int localId) {
        if (parent == null) {
            throw new IllegalStateException("Parent map is null!");
        }
        return convertTo(parent, localId);
    }

    /**
     * Converts an id local to this map to a foreign id, local to another map.
     *
     * @param other the other map
     * @param localId to convert
     * @return returns the foreign id, or 0 on failure
     */
    public int convertTo(StringToUniqueIntegerMap other, int localId) {
        if (other == null) {
            throw new IllegalStateException("Other map is null");
        }
        String localKey = store.reverseGet(localId);
        if (localKey == null) {
            throw new IllegalArgumentException("Cannot convert an id that is not registered locally.");
        }

        Integer foreignId = null;

        if (other == this) {
            return localId;
        } else if (other == parent) {
            foreignId = thisToParentMap.get(localId);
        } else if (other.parent == this) {
            foreignId = other.parentToThisMap.get(localId);
        }

        // Cache hit
        if (foreignId != null) {
            return foreignId;
        }

        Integer integerForeignId = other.store.get(localKey);

        // The other map doesn't have an entry for this key
        if (integerForeignId == null) {
            integerForeignId = other.register(localKey);
        }

        // Add the key/value pair to the cache
        if (other == parent) {
            thisToParentMap.set(localId, integerForeignId);
            parentToThisMap.set(integerForeignId, localId);
        } else if (other.parent == this) {
            other.thisToParentMap.set(integerForeignId, localId);
            other.parentToThisMap.set(localId, integerForeignId);
        }

        return integerForeignId;
    }

    /**
     * Converts a foreign id, local to a foreign map to an id local to this map.
     *
     * @param other the other map
     * @return returns the local id, or 0 on failure
     */
    public int convertFrom(StringToUniqueIntegerMap other, int foreignId) {
        return other.convertTo(this, foreignId);
    }

    /**
     * Registers a key with the map and returns the matching id.
     *
     * The id corresponding to a key will be consistent if registered more than once, including over restarts, subject to the persistence of the store.
     *
     * @param key the key to be added
     * @return returns the local id, or 0 on failure
     */
    public int register(String key) {
        Integer id = store.get(key);
        if (id != null) {
            return id;
        }

        int localId = nextId.getAndIncrement();

        while (localId < maxId) {
            if (store.setIfAbsent(key, localId)) {
                return localId;
            }

            Integer storeId = store.get(key);
            if (storeId != null) {
                return storeId;
            }

            localId = nextId.getAndIncrement();
        }

        throw new IllegalStateException("StringMap id space exhausted");
    }

    /**
     * Registers a key/id pair with the map.  If the id is already in use the method will fail.<br> <br> The id must be lower than the min id for the map to prevent clashing with the dynamically
     * allocated ids
     *
     * @param key the key to be added
     * @param id the desired id to be matched to the key
     * @return true if the key/id pair was successfully registered
     * @throws IllegalArgumentException if the id >= minId
     */
    public boolean register(String key, int id) {
        if (id >= this.minId) {
            throw new IllegalArgumentException("Hardcoded ids must be below the minimum id value");
        }

        return store.setIfAbsent(key, id);
    }

    /**
     * Gets the String corresponding to a given int.
     *
     * @return the String or null if no match
     */
    @Override
    public String getString(Integer value) {
        return store.reverseGet(value);
    }

    /**
     * Gets the int corresponding to a given String
     *
     * @param key The key
     * @return The int or null if no match
     */
    @Override
    public Integer getValue(String key) {
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
    public List<Pair<Integer, String>> getItems() {
        List<Pair<Integer, String>> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : store.getEntrySet()) {
            items.add(new ImmutablePair<>(entry.getValue(), entry.getKey()));
        }
        return items;
    }

    @Override
    public void clear() {
        while (this.nextId.getAndSet(minId) != minId) {
            if (this.parent != null) {
                for (int i = 0; i < maxId; i++) {
                    thisToParentMap.set(i, null);
                    parentToThisMap.set(i, null);
                }
            }
            store.clear();
        }
    }
}
