package com.arassec.igor.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility to format a stacktrace.
 */
public class StacktraceFormatter {

    /**
     * Prevents instantiation.
     */
    private StacktraceFormatter() {
    }

    /**
     * Formats the supplied stacktrace as String.
     *
     * @param throwable The throwable to format.
     *
     * @return The stacktrace as String.
     */
    public static String format(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

}
