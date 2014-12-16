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
package com.flowpowered.commons.console;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowpowered.commons.InterruptableInputStream;

/**
 * A JLine console wrapper.
 */
public class JLineConsole {
    public static final int INPUT_THREAD_YIELD = -1;
    public static final int INPUT_THREAD_BLOCK = -2;
    public static final int INPUT_THREAD_DEFAULT = Integer.getInteger("com.flowpowered.commons.console.inThreadSleepTime", isWindows() ? INPUT_THREAD_BLOCK : 10);

    protected static boolean isWindows() {
        String prop = System.getProperty("com.flowpowered.commons.console.forceOs");
        if (prop != null) {
            prop = prop.toLowerCase();
            if (prop.contains("windows")) {
                return true;
            }
            if (prop.contains("unix")) {
                return false;
            }
        }
        return SystemUtils.IS_OS_WINDOWS;
    }

    private final ConsoleReader reader;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final CommandCallback callback;
    private final Logger logger;
    private final ConsoleCommandThread commandThread;

    public JLineConsole(CommandCallback callback, Completer completer) {
        this(callback, completer, null);
    }

    public JLineConsole(CommandCallback callback, Completer completer, Logger logger) {
        this(callback, completer, logger, null, null);
    }

    public JLineConsole(CommandCallback callback, Completer completer, Logger logger, int inThreadSleepTime) {
        this(callback, completer, logger, inThreadSleepTime, null, null);
    }

    public JLineConsole(CommandCallback callback, Completer completer, Logger logger, OutputStream out, InputStream in) {
        this(callback, completer, logger, INPUT_THREAD_DEFAULT, out, in);
    }

    public JLineConsole(CommandCallback callback, Completer completer, Logger logger, int inThreadSleepTime, OutputStream out, InputStream in) {
        this.callback = callback;
        if (logger == null) {
            this.logger = LoggerFactory.getLogger("JLineConsole");
        } else {
            this.logger = logger;
        }
        if (out == null) {
            out = System.out;
        }
        if (in == null) {
            in = new FileInputStream(FileDescriptor.in);
        }

        if (inThreadSleepTime != INPUT_THREAD_BLOCK) {
            in = new InterruptableInputStream(in, inThreadSleepTime);
        }

        try {
            reader = new ConsoleReader(in, out);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        setupConsole(reader);

        @SuppressWarnings ("unchecked")
        final Collection<Completer> oldCompleters = reader.getCompleters();
        for (Completer c : new ArrayList<>(oldCompleters)) {
            reader.removeCompleter(c);
        }
        reader.addCompleter(completer);

        commandThread = new ConsoleCommandThread();
        commandThread.start();
    }

    public Logger getLogger() {
        return logger;
    }

    protected void setupConsole(ConsoleReader reader) {
    }

    private class ConsoleCommandThread extends Thread {
        public ConsoleCommandThread() {
            super("ConsoleCommandThread");
            setDaemon(true);
        }

        @Override
        public void run() {
            String command;
            while (!closed.get()) {
                try {
                    reader.print(String.valueOf(ConsoleReader.RESET_LINE));
                    command = reader.readLine(">", null);

                    if (command == null || command.trim().length() == 0) {
                        continue;
                    }

                    callback.handleCommand(command);
                } catch (InterruptedIOException e) {
                    // ignore
                } catch (Exception ex) {
                    // TODO: Maybe it should be error instead of warn?
                    logger.warn("Exception in console command thread:", ex);
                }
            }
        }
    }

    public void close() {
        if (closed.compareAndSet(false, true)) {
            closeImpl();
        }
    }

    protected void closeImpl() {
        try {
            commandThread.interrupt();
            reader.killLine();
            reader.print(String.valueOf(ConsoleReader.RESET_LINE));
            reader.flush();
        } catch (IOException ex) {
            // TODO: Maybe it should be warn instead of error?
            logger.error("Exception when trying to close console command input:", ex);
        }
    }

    public Thread getCommandThread() {
        return commandThread;
    }
}
