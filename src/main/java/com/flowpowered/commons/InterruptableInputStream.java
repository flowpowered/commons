package com.flowpowered.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

public class InterruptableInputStream extends InputStream {
    private final InputStream in;

    public InterruptableInputStream(InputStream in) {
        this.in = in;
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
            Thread.yield();
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
