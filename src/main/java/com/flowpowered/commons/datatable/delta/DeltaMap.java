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
package com.flowpowered.commons.datatable.delta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.flowpowered.commons.datatable.SerializableHashMap;
import com.flowpowered.commons.datatable.defaulted.DefaultedKey;

/**
 * This is a subclass of SerializableHashMap designed to mark delta elements. This is most needed for sub-maps. This map is serializable, however SerializableHashMap is not. Therefore, the no-arg
 * constructor of SerializableHashMap is called. This no-arg constructor should be nonsyncing. The GenericDatatableMap of super does not serialize the data by itself, so the writeObject and readObject
 * are implemented in this class to serialize the data.
 *
 * This classes manages setting the delta map of parent maps. This class supports null values, whereas SerializableHashMap does not.
 */
public class DeltaMap extends SerializableHashMap {
    private static final long serialVersionUID = 1L;
    private transient WeakReference<DeltaMap> reference = new WeakReference<>(this);
    private DeltaType type;
    private final String key;
    // If we have a parent, we aren't going to serialize it
    protected transient final DeltaMap parent;
    protected transient List<WeakReference<DeltaMap>> children = new ArrayList<>();

    public DeltaMap(DeltaType type) {
        this.type = type;
        this.key = null;
        this.parent = null;
    }

    public DeltaMap(DeltaMap parent, DeltaType type, String key) {
        this.type = type;
        this.parent = parent;
        this.parent.children.add(reference);
        this.key = key;

        // We want to update the parent for us
        this.parent.put(key, this);
    }

    public enum DeltaType {
        /*
         * Equivalent of calling clear() the putAll()
         */
        REPLACE,
        /*
         * Equivalent of calling putAll()
         */
        SET;
    }

    public DeltaType getType() {
        return type;
    }

    public void setType(DeltaType type) {
        this.type = type;
    }

    // SerializableHashMap does not permit null values; however, we need a niltype to show a deletion
    // Therefore, we need to override functionality:
    // - We never allow single reads: DeltaMap is used for whole-map updates
    // - If we add a value, update parent to notify it that it is dirty as well
    // - Since ConcurrentHashMap does not permit null values, we need a NILTYPE
    // - Values should never be removed from DeltaMap: if the owner removes a value, use DeltaMap.put(key, null)
    // - If we clear this map, the type changes to REPLACE and all elements are cleared

    @Override
    public <T extends Serializable> T get(Object key, T defaultValue) {
        throw new UnsupportedOperationException("DeltaMap must only be read in bulk.");
    }

    @Override
    public Serializable get(Object key) {
        throw new UnsupportedOperationException("DeltaMap must only be read in bulk.");
    }

    @Override
    public <T extends Serializable> T get(DefaultedKey<T> key) {
        throw new UnsupportedOperationException("DeltaMap must only be read in bulk.");
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        throw new UnsupportedOperationException("DeltaMap must only be read in bulk.");
    }

    @Override
    public Serializable remove(String key) {
        throw new UnsupportedOperationException("Values cannot be removed from DeltaMap");
    }

    @Override
    public Serializable putIfAbsent(String key, Serializable value) {
        updateParent();
        if (value == null) {
            value = NILTYPE;
        }
        return map.putIfAbsent(key, value);
    }

    @Override
    public Serializable put(String key, Serializable value) {
        updateParent();
        if (value == null) {
            value = NILTYPE;
        }
        return map.put(key, value);
    }

    @Override
    public void clear() {
        updateParent();
        setType(DeltaMap.DeltaType.REPLACE);
        map.clear();
    }

    @Override
    public void deserialize(byte[] data, boolean wipe) throws IOException {
        updateParent();
        if (wipe) {
            setType(DeltaType.REPLACE);
        }
        super.deserialize(data, wipe);
    }

    private void updateParent() {
        if (parent != null) {
            parent.put(this.key, this);
            parent.children.add(reference);
        }
    }

    public void reset() {
        type = DeltaType.SET;
        map.clear();
        for (Iterator<WeakReference<DeltaMap>> it = children.iterator(); it.hasNext(); ) {
            WeakReference<DeltaMap> c = it.next();
            if (c.get() == null) {
                it.remove();
                continue;
            }
            c.get().reset();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        reference = new WeakReference<>(this);
        children = new ArrayList<>();
    }
}
