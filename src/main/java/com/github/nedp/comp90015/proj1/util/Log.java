package com.github.nedp.comp90015.proj1.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convenience wrapper for the global logger
 * from java.util.logging.Logger.
 *
 * Created by nedp on 06/04/15.
 */
public class Log {
    private static Level LogLevel = Level.OFF;
    private static final Map<String, Logger> Loggers = new HashMap<>();

    public static void SetLevel(Level level) {
        synchronized (Loggers) {
            LogLevel = level;
            for (Logger logger : Loggers.values()) {
                logger.setLevel(level);
            }
        }
    }

    public static void S(String msg) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.SEVERE, frame.getClassName(), frame.getMethodName(), msg);
    }

    public static void W(String msg) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.WARNING, frame.getClassName(), frame.getMethodName(), msg);
    }

    public static void I(String msg) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.INFO, frame.getClassName(), frame.getMethodName(), msg);
    }

    public static void C(String msg) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.CONFIG, frame.getClassName(), frame.getMethodName(), msg);
    }

    public static void F(String msg) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.FINE, frame.getClassName(), frame.getMethodName(), msg);
    }

    public static void FF(String msg) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.FINER, frame.getClassName(), frame.getMethodName(), msg);
    }

    public static void FFF(String msg) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.FINEST, frame.getClassName(), frame.getMethodName(), msg);
    }

    public static void S(String msg, Throwable e) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.SEVERE, frame.getClassName(), frame.getMethodName(), msg, e);
    }

    public static void W(String msg, Throwable e) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.WARNING, frame.getClassName(), frame.getMethodName(), msg, e);
    }

    public static void I(String msg, Throwable e) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.INFO, frame.getClassName(), frame.getMethodName(), msg, e);
    }

    public static void C(String msg, Throwable e) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.CONFIG, frame.getClassName(), frame.getMethodName(), msg, e);
    }

    public static void F(String msg, Throwable e) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.FINE, frame.getClassName(), frame.getMethodName(), msg, e);
    }

    public static void FF(String msg, Throwable e) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.FINER, frame.getClassName(), frame.getMethodName(), msg, e);
    }

    public static void FFF(String msg, Throwable e) {
        final StackTraceElement frame = LastFrame();
        final Logger logger = DesiredLogger(frame);
        logger.logp(Level.FINEST, frame.getClassName(), frame.getMethodName(), msg, e);
    }

    private static Logger DesiredLogger(StackTraceElement frame) {
        synchronized (Loggers) {
            Logger logger = Loggers.get(frame.getClassName());
            if (logger == null) {
                logger = Logger.getLogger(frame.getClassName());
                logger.setLevel(LogLevel);

                for (Handler handler : logger.getHandlers()) {
                    logger.removeHandler(handler);
                }
                final Handler handler = new ConsoleHandler();
                handler.setLevel(Level.ALL);
                logger.addHandler(handler);
                logger.setUseParentHandlers(false);

                Loggers.put(frame.getClassName(), logger);
            }
            return logger;
        }
    }

    private static StackTraceElement LastFrame() {
        return Thread.currentThread().getStackTrace()[3];
    }
}
