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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * This implements a SimpleStore that is stored in memory. The save and load methods can be used to write the map to a binary file.
 */
public class BinaryFileStore extends MemoryStore<Integer> {
    private Path path;
    private boolean dirty = true;

    public BinaryFileStore(Path path) {
        super();
        this.path = path;
    }

    public BinaryFileStore() {
        this(null);
    }

    public synchronized void setPath(Path path) {
        this.path = path;
    }

    public synchronized Path getPath() {
        return path;
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

        boolean saved = true;
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)));
            Iterator<Entry<String, Integer>> itr = super.getEntrySet().iterator();

            while (itr.hasNext()) {
                Entry<String, Integer> next = itr.next();
                out.writeInt(next.getValue());
                out.writeUTF(next.getKey());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            saved = false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                saved = false;
            }
            if (saved) {
                dirty = false;
            }
        }
        return saved;
    }

    @Override
    public synchronized boolean load() {
        boolean loaded = true;
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)));

            boolean eof = false;
            while (!eof) {
                try {
                    Integer id = in.readInt();
                    String key = in.readUTF();
                    set(key, id);
                } catch (EOFException eofe) {
                    eof = true;
                }
            }
        } catch (IOException ioe) {
            loaded = false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
                loaded = false;
            }
        }
        if (loaded) {
            dirty = false;
        }
        return loaded;
    }

    @Override
    public synchronized Integer remove(String key) {
        Integer value = super.remove(key);
        if (value != null) {
            dirty = true;
        }
        return value;
    }

    @Override
    public synchronized Integer set(String key, Integer value) {
        dirty = true;
        return super.set(key, value);
    }
}
