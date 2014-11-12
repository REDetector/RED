package com.xl.exception;

import com.xl.net.crashreport.CrashReporter;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * The error catcher can be attached to the main JVM and is triggered any time a
 * throwable exception makes it back all the way through the stack without being
 * caught so we don't miss any errors.
 */
public class ErrorCatcher implements UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread arg0, Throwable arg1) {
        new CrashReporter(arg1);
    }

}
