package com.flowpowered.commons.console;

import java.io.InputStream;
import java.io.OutputStream;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

import com.github.wolf480pl.jline_log4j2_appender.ConsoleSetupMessage;
import com.github.wolf480pl.jline_log4j2_appender.ConsoleSetupMessage.Action;

/**
 * Requires you to add jline-log4j2-appender to your dependencies (here it's just an optional dependency, which means it's not transitive).
 */
public class Log4j2JLineConsole extends JLineConsole {
    private ConsoleReader reader;

    public Log4j2JLineConsole(CommandCallback callback, Completer completer) {
        super(callback, completer);
    }

    public Log4j2JLineConsole(CommandCallback callback, Completer completer, Logger logger) {
        super(callback, completer, logger);
    }

    public Log4j2JLineConsole(CommandCallback callback, Completer completer, Logger logger, int inThreadSleepTime) {
        super(callback, completer, logger, inThreadSleepTime);
    }

    public Log4j2JLineConsole(CommandCallback callback, Completer completer, Logger logger, OutputStream out, InputStream in) {
        super(callback, completer, logger, out, in);
    }

    public Log4j2JLineConsole(CommandCallback callback, Completer completer, Logger logger, int inThreadSleepTime, OutputStream out, InputStream in) {
        super(callback, completer, logger, inThreadSleepTime, out, in);
    }

    @Override
    public void setupConsole(ConsoleReader reader) {
        this.reader = reader;
        String loggerName = getLogger().getName();
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(loggerName);
        logger.log(Level.INFO, new ConsoleSetupMessage(reader, "Setting up console"));
    }

    @Override
    protected void closeImpl() {
        super.closeImpl();
        String loggerName = getLogger().getName();
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(loggerName);
        logger.log(Level.INFO, new ConsoleSetupMessage(reader, Action.REMOVE, "Closing console"));
    }

}
