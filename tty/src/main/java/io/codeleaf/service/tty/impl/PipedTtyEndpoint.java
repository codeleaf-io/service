package io.codeleaf.service.tty.impl;

import io.codeleaf.service.tty.TtyConnection;
import io.codeleaf.service.tty.TtyEndpoint;

import java.io.*;

public final class PipedTtyEndpoint implements TtyEndpoint {

    private final PrintStream stdin;
    private final InputStream stdout;
    private final InputStream stderr;
    private final TtyConnection connection;

    public static PipedTtyEndpoint create() throws IOException {
        PipedOutputStream inPipe = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(inPipe);
        PipedOutputStream outPipe = new PipedOutputStream();
        PipedInputStream stdout = new PipedInputStream(outPipe);
        PipedOutputStream errPipe = new PipedOutputStream();
        PipedInputStream stderr = new PipedInputStream(errPipe);
        return new PipedTtyEndpoint(new PrintStream(inPipe), stdout, stderr,
                new DefaultTtyConnection(in, new PrintStream(outPipe), new PrintStream(errPipe)));
    }

    PipedTtyEndpoint(PrintStream stdin, InputStream stdout, InputStream stderr, DefaultTtyConnection connection) {
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
        this.connection = connection;
    }

    @Override
    public PrintStream getStdin() {
        return stdin;
    }

    @Override
    public InputStream getStdout() {
        return stdout;
    }

    @Override
    public InputStream getStderr() {
        return stderr;
    }

    public TtyConnection getConnection() {
        return connection;
    }

    @Override
    public void close() throws IOException {
        stdin.close();
        stdout.close();
        stderr.close();
        connection.close();
    }

}
