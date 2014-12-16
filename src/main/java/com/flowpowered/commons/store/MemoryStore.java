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
package com.flowpowered.commons.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.Validate;

/**
 * This implements a SimpleStore that is stored in memory. It is not persisted between restarts.
 */
public class MemoryStore<T> implements SimpleStore<T> {
    private final Map<String, T> map;
    private final Map<T, String> reverseMap;

    public MemoryStore() {
        map = new HashMap<>();
        reverseMap = new HashMap<>();
    }

    @Override
    public synchronized boolean save() {
        return true;
    }

    @Override
    public synchronized boolean load() {
        return true;
    }

    @Override
    public synchronized Collection<String> getKeys() {
        return map.keySet();
    }

    @Override
    public synchronized Set<Entry<String, T>> getEntrySet() {
        return map.entrySet();
    }

    @Override
    public synchronized int getSize() {
        return map.size();
    }

    @Override
    public synchronized boolean clear() {
        map.clear();
        reverseMap.clear();
        return true;
    }

    @Override
    public synchronized T get(String key) {
        return map.get(key);
    }

    @Override
    public synchronized String reverseGet(T value) {
        return reverseMap.get(value);
    }

    @Override
    public synchronized T get(String key, T def) {
        T value = get(key);
        if (value == null) {
            return def;
        }
        return value;
    }

    @Override
    public synchronized T remove(String key) {
        T value = map.remove(key);
        if (value != null) {
            reverseMap.remove(value);
        }
        return value;
    }

    @Override
    public synchronized T set(String key, T value) {
        Validate.notNull(key);
        Validate.notNull(value);

        T oldValue = map.put(key, value);
        if (oldValue != null) {
            reverseMap.remove(oldValue);
        }
        reverseMap.put(value, key);
        return oldValue;
    }

    @Override
    public synchronized boolean setIfAbsent(String key, T value) {
        if (map.get(key) != null) {
            return false;
        }

        if (reverseMap.get(value) != null) {
            return false;
        }

        set(key, value);
        return true;
    }
}
