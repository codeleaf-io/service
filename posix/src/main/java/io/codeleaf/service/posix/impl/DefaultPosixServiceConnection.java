package io.codeleaf.service.posix.impl;

import io.codeleaf.service.posix.PosixServiceConnection;
import io.codeleaf.service.posix.PosixServiceEndpoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class DefaultPosixServiceConnection implements PosixServiceConnection {

    private final Process process;
    private final OutputStream stdin;
    private final InputStream stdout;
    private final InputStream stderr;
    private final UUID uuid;
    private final PosixServiceEndpoint endpoint;

    public DefaultPosixServiceConnection(Process process, OutputStream stdin, InputStream stdout, InputStream stderr, UUID uuid, PosixServiceEndpoint endpoint) {
        this.process = process;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
        this.uuid = uuid;
        this.endpoint = endpoint;
    }

    @Override
    public boolean isTerminated() {
        return !process.isAlive();
    }

    @Override
    public int getExitValue() {
        if (process.isAlive()) {
            throw new IllegalStateException();
        }
        return process.exitValue();
    }

    @Override
    public OutputStream getStdin() {
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

    @Override
    public void signal(int signal) {
        try {
            String[] cmdarray = new String[]{"kill", "-" + signal, String.valueOf(process.pid())};
            Runtime.getRuntime().exec(cmdarray);
        } catch (IOException cause) {
            throw new IllegalStateException("Failed to signal!");
        }
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public PosixServiceEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public void close() throws IOException {
        stdin.close();
        stdout.close();
        stderr.close();
    }
}
