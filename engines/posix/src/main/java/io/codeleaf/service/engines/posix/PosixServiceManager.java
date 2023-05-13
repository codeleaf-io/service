package io.codeleaf.service.engines.posix;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.common.utils.IdentityBuilder;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.posix.PosixService;
import io.codeleaf.service.posix.PosixServiceConnection;
import io.codeleaf.service.posix.PosixServiceDefinition;
import io.codeleaf.service.posix.PosixServiceEndpoint;
import io.codeleaf.service.posix.impl.DefaultPosixService;
import io.codeleaf.service.posix.impl.DefaultPosixServiceEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public final class PosixServiceManager {

    private static final Logger LOG = LoggerFactory.getLogger(PosixServiceManager.class);

    private final List<PosixServiceConnection> connections = new LinkedList<>();

    private Process process;
    private PosixService service;

    private final PosixServiceDefinition definition;

    public PosixServiceManager(PosixServiceDefinition definition) {
        this.definition = definition;
    }

    public PosixServiceDefinition getDefinition() {
        return definition;
    }

    public Process getProcess() {
        return process;
    }

    public PosixService getService() {
        return service;
    }

    public List<PosixServiceConnection> getConnections() {
        return connections;
    }

    public synchronized void start(PosixServiceEngine engine) throws ServiceException {
        if (process != null) {
            throw new IllegalStateException("Already initialized!");
        }
        try {
            Path servicePath = engine.getEngineTempPath().resolve("services").resolve(getDefinition().getUUID().toString());
            Files.createDirectories(servicePath);
            Path workdir = servicePath.resolve("workdir");
            Files.createDirectory(workdir);
            LOG.info("Created service directory at: " + servicePath);
            Path stdinPath = PosixFifos.createFifo(servicePath, "stdin");
            Path stdoutPath = PosixFifos.createFifo(servicePath, "stdout");
            Path stderrPath = PosixFifos.createFifo(servicePath, "stderr");
            AsynchronousFileChannel stdinChannel = AsynchronousFileChannel.open(servicePath.resolve("stdin"), StandardOpenOption.WRITE);
            AsynchronousFileChannel stdoutChannel = AsynchronousFileChannel.open(servicePath.resolve("stdout"), StandardOpenOption.READ);
            AsynchronousFileChannel stderrChannel = AsynchronousFileChannel.open(servicePath.resolve("stderr"), StandardOpenOption.READ);
            process = new ProcessBuilder().directory(workdir.toFile()).command(definition.getCommand()).redirectInput(stdinPath.toFile()).redirectOutput(stdoutPath.toFile()).redirectError(stderrPath.toFile()).start();
            long pid = process.pid();
            engine.getHandler().registerService(pid, stdinChannel, stdoutChannel, stderrChannel);
            Files.writeString(servicePath.resolve("pid"), String.valueOf(pid));
            Files.writeString(servicePath.resolve("command"), getDefinition().getName());
            String name = InetAddress.getLocalHost().getCanonicalHostName() + ":" + pid;
            Identification identification = new IdentityBuilder().withName(name).build();
            PosixServiceEndpoint endpoint = new DefaultPosixServiceEndpoint(pid);
            service = new DefaultPosixService(engine, definition, identification, endpoint);
        } catch (IOException cause) {
            throw new ServiceException("Failed to start service: " + cause, cause);
        }
    }

    public synchronized void suspend() {
        PosixSignals.suspend(process.pid());
    }

    public synchronized void resume() {
        PosixSignals.resume(process.pid());
    }

    public synchronized void stop() throws ServiceException {
        try {
            long pid = process.pid();
            PosixSignals.terminate(process.pid());
            try {
                Thread.sleep(10_000L);
            } catch (InterruptedException ignored) {
            }
            if (process.isAlive()) {
                PosixSignals.kill(process.pid());
            }
            PosixServiceEngine engine = (PosixServiceEngine) service.getEngine();
            engine.getHandler().unregisterService(pid);
            Path servicePath = engine.getEngineTempPath().resolve("services").resolve(getDefinition().getUUID().toString());
            Files.walk(servicePath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException cause) {
            throw new ServiceException("Failed to stop service: " + cause, cause);
        }
    }

    public synchronized void ensureStarted(PosixServiceEngine engine) throws ServiceException {
        if (process == null) {
            start(engine);
        }
    }

    public synchronized void ensureStopped() throws ServiceException {
        if (process != null && process.isAlive()) {
            stop();
        }
    }

    public synchronized boolean isStarted() {
        return process.isAlive();
    }

    public synchronized PosixServiceConnection connect() throws ServiceException {
        try {
            PosixServiceEngine engine = (PosixServiceEngine) service.getEngine();
            PosixPipedServiceConnection connection = PosixPipedServiceConnection.create(process, engine, service.getEndpoint());
            engine.getHandler().connect(connection);
            return connection;
        } catch (IOException cause) {
            throw new ServiceException("Failed to connect: " + cause, cause);
        }
    }

}
