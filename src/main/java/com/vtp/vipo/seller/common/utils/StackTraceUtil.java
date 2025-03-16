package com.vtp.vipo.seller.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Author: hieuhm12
 * Date: 9/19/2024
 */
public class StackTraceUtil {
    private static final String NULL_STRING = "null";

    public static String stackTrace(final Throwable t) {
        if (t == null) {
            return NULL_STRING;
        }

        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final PrintStream ps = new PrintStream(out)) {
            t.printStackTrace(ps);
            ps.flush();
            return out.toString();
        } catch (final IOException ignored) {
            // ignored
        }
        return NULL_STRING;
    }

    private StackTraceUtil() {
    }
}
