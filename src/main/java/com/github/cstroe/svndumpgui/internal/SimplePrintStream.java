package com.github.cstroe.svndumpgui.internal;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Does not swallow exceptions, unlike {@link java.io.PrintStream}, but
 * converts the checked IOException into unchecked RuntimeException, so
 * you can use it like PrintStream.
 */
public class SimplePrintStream extends FilterOutputStream {
    private String lineSeparator;

    public SimplePrintStream(OutputStream out) {
        super(out);
        if(out == null) {
            throw new IllegalArgumentException("Cannot write to a null OutputStream.");
        }
        this.lineSeparator = System.getProperty("line.separator");
    }

    public SimplePrintStream(OutputStream out, String lineSeparator) {
        super(out);
        if(lineSeparator == null) {
            throw new IllegalArgumentException("Null line separator is not allowed.  Please check the 'line.separator' system property.");
        }
        this.lineSeparator = lineSeparator;
    }

    public void print(String output) {
        try {
            write(output.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void println() {
        print(lineSeparator);
    }

    public void println(String output) {
        print(output);
        println();
    }

    public void println(int output) {
        println(Integer.toString(output));
    }

    public void print(int output) {
        print(Integer.toString(output));
    }

    @Override
    public void flush() {
        try {
            super.flush();
        } catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
