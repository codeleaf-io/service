package io.codeleaf.service.tty.impl;

import io.codeleaf.service.tty.TtyConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class DefaultTtyConnection implements TtyConnection {

    private final InputStream in;
    private final PrintStream out;
    private final PrintStream err;

    public DefaultTtyConnection(InputStream in, PrintStream out, PrintStream err) {
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public InputStream getIn() {
        return in;
    }

    public PrintStream getOut() {
        return out;
    }

    public PrintStream getErr() {
        return err;
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        err.close();
    }

}
