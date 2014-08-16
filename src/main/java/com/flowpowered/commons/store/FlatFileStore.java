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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import com.flowpowered.commons.PathUtil;

/**
 * This implements a SimpleStore that is stored in memory. The save and load methods can be used to write the map to a File.
 */
public class FlatFileStore<T> extends MemoryStore<T> {
    private final Path path;
    private boolean dirty = false;
    private final Class<?> clazz; // preserve class, so parser knows what to do

    public FlatFileStore(Path path, Class<?> clazz) {
        super();
        this.clazz = clazz;
        this.path = path;
        if (path != null) {
            if (!Files.exists(path)) {
                try {
                    Files.createFile(path);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public synchronized boolean clear() {
        dirty = true;
        return super.clear();
    }

    @Override
    public synchronized boolean save() {
        if (!dirty) {
            return true;
        }

        Collection<String> strings = getStrings();
        boolean saved = PathUtil.stringToFile(strings, path);
        if (saved) {
            dirty = false;
        }

        return saved;
    }

    @Override
    public synchronized boolean load() {
        Collection<String> strings = PathUtil.fileToString(path);
        if (strings == null) {
            return false;
        }
        boolean loaded = processStrings(strings);
        if (loaded) {
            dirty = false;
        }
        return loaded;
    }

    @Override
    public synchronized T remove(String key) {
        T value = super.remove(key);
        if (value != null) {
            dirty = true;
        }
        return value;
    }

    @Override
    public synchronized T set(String key, T value) {
        dirty = true;
        return super.set(key, value);
    }

    private synchronized Collection<String> getStrings() {
        Iterator<Entry<String, T>> itr = super.getEntrySet().iterator();
        ArrayList<String> strings = new ArrayList<>(super.getSize());
        while (itr.hasNext()) {
            Entry<String, T> entry = itr.next();
            String encodedKey = encode(entry.getKey());
            T value = entry.getValue();
            strings.add(value + ":" + encodedKey);
        }
        return strings;
    }

    private boolean processStrings(Collection<String> strings) {
        super.clear();
        for (String string : strings) {
            String[] split = string.trim().split(":");
            if (split.length != 2) {
                return false;
            }
            T value;
            try {
                value = parse(split[0]);
            } catch (NumberFormatException nfe) {
                return false;
            }
            String key = decode(split[1]);
            set(key, value);
        }
        return true;
    }

    private static String encode(String key) {
        String encoded = key;
        encoded = encoded.replace("\\", "\\\\");
        encoded = encoded.replace("\n", "\\n");
        encoded = encoded.replace(":", "\\:");
        return encoded;
    }

    private static String decode(String encoded) {
        String key = encoded;
        key = key.replace("\\:", ":");
        key = key.replace("\\n", "\n");
        key = key.replace("\\\\", "\\");
        return encoded;
    }

    @SuppressWarnings ("unchecked")
    private T parse(String string) {
        if (clazz.equals(Integer.class)) {
            return (T) (Object) Integer.parseInt(string);
        } else if (clazz.equals(String.class)) {
            return (T) string;
        } else {
            throw new IllegalArgumentException("Unable to parse clazzes of type " + clazz.getName());
        }
    }
}
