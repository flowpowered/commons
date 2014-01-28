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
package com.flowpowered.commons.console;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.NullCompleter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowpowered.commons.InterruptableInputStream;


/**
 * A JLine console wrapper.
 */
public class JLineConsole {
    private final ConsoleReader reader;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final CommandCallback callback;
    private final Logger logger;
    private final ConsoleCommandThread commandThread;

    public JLineConsole(CommandCallback callback, List<Completer> completers) {
        this(callback, completers, LoggerFactory.getLogger("JLineConsole"));
    }

    public JLineConsole(CommandCallback callback, List<Completer> completers, Logger logger) {
        this.callback = callback;
        this.logger = logger;

        try {
            reader = new ConsoleReader(new InterruptableInputStream(new FileInputStream(FileDescriptor.in)), System.out);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        setupConsole(reader);

        @SuppressWarnings ("unchecked")
        final Collection<Completer> completer = reader.getCompleters();
        for (Completer c : new ArrayList<>(completer)) {
            reader.removeCompleter(c);
        }
        Completer[] list = completers.toArray(new Completer[completer.size() + 1]);
        list[list.length - 1] = new NullCompleter();
        reader.addCompleter(new ArgumentCompleter(list));

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
            reader.setCursorPosition(0);
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
