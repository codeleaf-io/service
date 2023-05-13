package io.codeleaf.service.engines.posix;

import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.posix.PosixServiceConnection;
import io.codeleaf.service.posix.PosixServiceEndpoint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.UUID;
import java.util.function.Supplier;

public final class PosixPipedServiceConnection implements PosixServiceConnection, PosixServiceIOHandler.Output {

    private volatile boolean closed;

    private final Supplier<Integer> exitValue;
    private final Pipe stdoutPipe;
    private final Pipe stderrPipe;
    private final UUID uuid;
    private final PosixServiceEngine engine;
    private final PosixServiceEndpoint endpoint;

    private volatile PosixServiceIOHandler.Input input;

    public PosixPipedServiceConnection(Supplier<Integer> exitValue, Pipe stdoutPipe, Pipe stderrPipe, UUID uuid, PosixServiceEngine engine, PosixServiceEndpoint endpoint) {
        this.exitValue = exitValue;
        this.stdoutPipe = stdoutPipe;
        this.stderrPipe = stderrPipe;
        this.uuid = uuid;
        this.engine = engine;
        this.endpoint = endpoint;
    }

    public static PosixPipedServiceConnection create(Process process, PosixServiceEngine engine, PosixServiceEndpoint endpoint) throws IOException {
        return new PosixPipedServiceConnection(() -> process.isAlive() ? process.exitValue() : null, Pipe.open(), Pipe.open(), UUID.randomUUID(), engine, endpoint);
    }

    public void setInput(PosixServiceIOHandler.Input input) {
        this.input = input;
    }

    public long getPid() {
        return endpoint.getPid();
    }

    @Override
    public boolean isTerminated() {
        return exitValue.get() != null;
    }

    @Override
    public int getExitValue() {
        Integer value = exitValue.get();
        if (value == null) {
            throw new IllegalStateException("Not finished!");
        }
        return value;
    }

    @Override
    public void doInput(ByteBuffer buffer) throws ServiceException {
        try {
            input.doInput(buffer);
        } catch (IOException cause) {
            throw new ServiceException(cause);
        }
    }

    @Override
    public Pipe.SourceChannel getStdout() {
        return stdoutPipe.source();
    }

    @Override
    public Pipe.SourceChannel getStderr() {
        return stderrPipe.source();
    }

    @Override
    public void signal(int signal) throws ServiceException {
        try {
            input.doSignal(signal);
        } catch (IOException cause) {
            throw new ServiceException(cause);
        }
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public ServiceEngine getEngine() {
        return engine;
    }

    @Override
    public PosixServiceEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        try {
            stdoutPipe.sink().close();
            stdoutPipe.source().close();
            stderrPipe.sink().close();
            stderrPipe.source().close();
            engine.getHandler().disconnect(this);
        } finally {
            closed = true;
        }
    }

    @Override
    public void doOutput(ByteBuffer buffer) throws IOException {
        stdoutPipe.sink().write(buffer);
    }

    @Override
    public void doError(ByteBuffer buffer) throws IOException {
        stderrPipe.sink().write(buffer);
    }
}
