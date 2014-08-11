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
package com.flowpowered.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

public class InterruptableInputStream extends InputStream {
    private final InputStream in;
    private final long sleepTime;

    public InterruptableInputStream(InputStream in) {
        this(in, -1);
    }

    public InterruptableInputStream(InputStream in, long sleepTime) {
        this.in = in;
        this.sleepTime = sleepTime;
    }

    @Override
    public int read() throws IOException {
        while (true) {
            if (in.available() > 0) {
                return in.read();
            }
            if (Thread.interrupted()) {
                throw new InterruptedIOException();
            }
            if (sleepTime < 0) {
                Thread.yield();
            } else {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new InterruptedIOException();
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
