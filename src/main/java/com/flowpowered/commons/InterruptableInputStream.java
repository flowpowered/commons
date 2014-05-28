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
