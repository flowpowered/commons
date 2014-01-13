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
package com.flowpowered.commons.datatable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.flowpowered.commons.datatable.delta.DeltaMap;

public class ManagedHashMap extends SerializableHashMap implements ManagedMap {
    private static final long serialVersionUID = 1L;
    private final DeltaMap delta;

    public ManagedHashMap() {
        this.delta = new DeltaMap(DeltaMap.DeltaType.SET);
    }

    public ManagedHashMap(ManagedHashMap parent, String key) {
        this.delta = new DeltaMap(parent.delta, DeltaMap.DeltaType.SET, key);
    }

    @Override
    public Serializable putIfAbsent(String key, Serializable value) {
        delta.putIfAbsent(key, value);
        return super.putIfAbsent(key, value);
    }

    @Override
    public Serializable put(String key, Serializable value) {
        delta.putIfAbsent(key, value);
        return super.put(key, value);
    }

    @Override
    public Serializable remove(String key) {
        delta.put(key, null);
        return map.remove(key);
    }

    @Override
    public void clear() {
        delta.clear();
        map.clear();
    }

    @Override
    public void deserialize(byte[] data, boolean wipe) throws IOException {
        this.deserialize(data, wipe, true);
    }

    @Override
    public void deserialize(byte[] data, boolean wipe, boolean updateDelta) throws IOException {
        if (updateDelta) { 
            delta.deserialize(data, wipe);
        }
        super.deserialize(data, wipe);
    }

    /**
     * This will return if the map has been map has been modified since the last call to setDirty(false).
     *
     * @return the dirty state of the map
     */
    @Override
    public DeltaMap getDeltaMap() {
        return delta;
    }

    @Override
    public void resetDelta() {
        delta.reset();
    }

    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder("ManagedHashMap {");
        for (Map.Entry<? extends String, ? extends Serializable> e : entrySet()) {
            toString.append("(");
            toString.append(e.getKey());
            toString.append(", ");
            toString.append(e.getValue());
            toString.append("), ");
        }
        toString.delete(toString.length() - 2, toString.length());
        toString.append("}");
        return toString.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        for (Map.Entry<? extends String, ? extends Serializable> e : entrySet()) {
            builder.append(e.getKey());
            builder.append(e.getValue());
        }
        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ManagedHashMap)) {
            return false;
        }

        ManagedHashMap other = (ManagedHashMap) obj;
        if (isEmpty() && other.isEmpty()) {
            return true;
        }

        for (Map.Entry<? extends String, ? extends Serializable> e : entrySet()) {
            Serializable value = e.getValue();
            Serializable otherValue = other.get(e.getKey());
            if (value != null) {
                if (!value.equals(otherValue)) {
                    return false;
                }
            } else if (otherValue != null) {
                return false;
            }
        }
        return true;
    }
}
