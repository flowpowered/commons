package com.flowpowered.commons.console;

import java.util.List;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

import com.github.wolf480pl.jline_log4j2_appender.ConsoleSetupMessage;

/**
 * Requires you to add jline-log4j2-appender to your dependencies (here it's just an optional dependency, which means it's not transitive).
 */
public class Log4j2JLineConsole extends JLineConsole {

    public Log4j2JLineConsole(CommandCallback callback, List<Completer> completers) {
        super(callback, completers);
    }

    public Log4j2JLineConsole(CommandCallback callback, List<Completer> completers, Logger logger) {
        super(callback, completers, logger);
    }

    @Override
    public void setupConsole(ConsoleReader reader) {
        String loggerName = getLogger().getName();
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(loggerName);
        logger.log(Level.INFO, new ConsoleSetupMessage(reader, "Setting up console"));
    }

}
