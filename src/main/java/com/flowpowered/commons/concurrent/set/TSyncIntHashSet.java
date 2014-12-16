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
package com.flowpowered.commons.concurrent.set;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.flowpowered.math.GenericMath;
import gnu.trove.TIntCollection;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * This is a synchronised version of the Trove IntHashSet.
 *
 * Read/write locks are used to synchronise access.
 *
 * By default, it creates 16 sub-maps and there is a separate read/write lock for each submap.
 */
public class TSyncIntHashSet implements TIntSet {
    private final int setCount;
    private final int setMask;
    private final int hashScramble;
    private final ReadWriteLock[] lockArray;
    private final TIntHashSet[] setArray;
    private final int no_entry_value;
    private final AtomicInteger totalValues = new AtomicInteger(0);

    /**
     * Creates a synchronised map based on the Trove long object map
     */
    public TSyncIntHashSet() {
        this(16);
    }

    /**
     * Creates a synchronised set based on the Trove long object map
     *
     * @param setCount the number of sub-sets
     */
    public TSyncIntHashSet(int setCount) {
        this(setCount, 32);
    }

    /**
     * Creates a synchronised set based on the Trove long object map
     *
     * @param setCount the number of sub-maps
     * @param initialCapacity the initial capacity of the map
     */
    public TSyncIntHashSet(int setCount, int initialCapacity) {
        this(setCount, initialCapacity, 0.5F);
    }

    /**
     * Creates a synchronised set based on the Trove long object map
     *
     * @param setCount the number of sub-maps
     * @param initialCapacity the initial capacity of the map
     * @param loadFactor the load factor for the map
     */
    public TSyncIntHashSet(int setCount, int initialCapacity, float loadFactor) {
        this(setCount, initialCapacity, loadFactor, Constants.DEFAULT_INT_NO_ENTRY_VALUE);
    }

    /**
     * Creates a synchronised set based on the Trove long object map
     *
     * @param setCount the number of sub-maps
     * @param initialCapacity the initial capacity of the map
     * @param loadFactor the load factor for the map
     * @param noEntryValue the value used to indicate a null key
     */
    public TSyncIntHashSet(int setCount, int initialCapacity, float loadFactor, int noEntryValue) {
        if (setCount > 0x100000) {
            throw new IllegalArgumentException("Set count exceeds valid range");
        }
        setCount = GenericMath.roundUpPow2(setCount);
        setMask = setCount - 1;
        this.setCount = setCount;
        this.hashScramble = (setCount << 8) + 1;
        setArray = new TIntHashSet[setCount];
        lockArray = new ReadWriteLock[setCount];
        for (int i = 0; i < setCount; i++) {
            setArray[i] = new TIntHashSet(initialCapacity / setCount, loadFactor, noEntryValue);
            lockArray[i] = new ReentrantReadWriteLock();
        }
        this.no_entry_value = noEntryValue;
    }

    @Override
    public void clear() {
        for (int m = 0; m < setCount; m++) {
            clear(m);
        }
    }

    private void clear(int m) {
        Lock lock = lockArray[m].writeLock();
        lock.lock();
        try {
            totalValues.addAndGet(-setArray[m].size());
            setArray[m].clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean contains(int value) {
        for (int m = 0; m < setCount; m++) {
            if (containsValue(m, value)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsValue(int m, int value) {
        Lock lock = lockArray[m].readLock();
        lock.lock();
        try {
            return setArray[m].contains(value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getNoEntryValue() {
        return no_entry_value;
    }

    @Override
    public boolean isEmpty() {
        return totalValues.get() == 0;
    }

    @Override
    public int[] toArray(int[] dest) {
        for (int m = 0; m < setCount; m++) {
            lockArray[m].readLock().lock();
        }
        try {
            int localSize = totalValues.get();
            int[] keys;
            if (dest == null || dest.length < localSize) {
                keys = new int[localSize];
            } else {
                keys = dest;
            }
            int position = 0;
            for (int m = 0; m < setCount; m++) {
                int[] mapKeys = setArray[m].toArray();
                for (int mapKey : mapKeys) {
                    keys[position++] = mapKey;
                }
            }
            if (position != localSize) {
                throw new IllegalStateException("Key counter does not match actual total map size");
            }
            return keys;
        } finally {
            for (int m = 0; m < setCount; m++) {
                lockArray[m].readLock().unlock();
            }
        }
    }

    @Override
    public int[] toArray() {
        return toArray(null);
    }

    @Override
    public boolean add(int entry) {
        int m = setHash(entry);
        Lock lock = lockArray[m].writeLock();
        lock.lock();
        try {
            boolean success = setArray[m].add(entry);
            if (success) {
                totalValues.incrementAndGet();
            }
            return success;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(int key) {
        int m = setHash(key);
        Lock lock = lockArray[m].writeLock();
        lock.lock();
        try {
            boolean success = setArray[m].remove(key);
            if (success) {
                totalValues.decrementAndGet();
            }
            return success;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        return totalValues.get();
    }

    private int setHash(long key) {
        int intKey = (int) (key >> 32 ^ key);

        return (0x7FFFFFFF & intKey) % hashScramble & setMask;
    }

    @Override
    public TIntIterator iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(TIntCollection collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(int[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Integer> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(TIntCollection collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(TIntCollection collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(int[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(TIntCollection collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(int[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean forEach(TIntProcedure procedure) {
        throw new UnsupportedOperationException();
    }
}
