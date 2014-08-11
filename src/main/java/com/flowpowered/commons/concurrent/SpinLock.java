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
package com.flowpowered.commons.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * A non-reentrant spin lock.<br> <br> The lock will spin 100 times before falling back to using wait/notify.
 */
public class SpinLock implements Lock {
    private static int MAX_SPINS = 100;
    private AtomicBoolean locked = new AtomicBoolean();
    private AtomicInteger waiting = new AtomicInteger();

    @Override
    public void lock() {
        for (int i = 0; i < MAX_SPINS; i++) {
            if (tryLock()) {
                return;
            }
        }

        boolean interrupted = false;
        boolean success = false;

        try {
            while (!success) {
                try {
                    waitLock();
                    success = true;
                } catch (InterruptedException ie) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        for (int i = 0; i < MAX_SPINS; i++) {
            if (tryLock()) {
                return;
            }
        }
        waitLock();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long endTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(time, unit) + 1;
        boolean timedOut = false;
        while (!tryLock()) {
            timedOut = System.currentTimeMillis() >= endTime;
            if (timedOut) {
                break;
            }
        }
        return !timedOut;
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock() {
        return locked.compareAndSet(false, true);
    }

    private void waitLock() throws InterruptedException {
        waiting.incrementAndGet();
        try {
            synchronized (waiting) {
                while (!tryLock()) {
                    waiting.wait();
                }
            }
        } finally {
            waiting.decrementAndGet();
        }
    }

    @Override
    public void unlock() {
        if (!locked.compareAndSet(true, false)) {
            throw new IllegalStateException("Attempt to unlock lock when it isn't locked");
        }
        if (!waiting.compareAndSet(0, 0)) {
            synchronized (waiting) {
                waiting.notifyAll();
            }
        }
    }

    public static void dualLock(Lock a, Lock b) {
        if (a == b) {
            a.lock();
            return;
        }

        while (true) {
            a.lock();
            if (b.tryLock()) {
                return;
            }
            a.unlock();
            b.lock();
            if (a.tryLock()) {
                return;
            }
            b.unlock();
        }
    }

    public static void dualUnlock(Lock a, Lock b) {
        try {
            a.unlock();
        } finally {
            if (b != a) {
                b.unlock();
            }
        }
    }
}

