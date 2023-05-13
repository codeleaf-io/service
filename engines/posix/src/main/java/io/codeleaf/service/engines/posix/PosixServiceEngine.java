package io.codeleaf.service.engines.posix;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.service.*;
import io.codeleaf.service.posix.PosixService;
import io.codeleaf.service.posix.PosixServiceConnection;
import io.codeleaf.service.posix.PosixServiceDefinition;
import io.codeleaf.service.posix.PosixServiceEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class PosixServiceEngine implements ServiceEngine {

    private static final Logger LOG = LoggerFactory.getLogger(PosixServiceEngine.class);

    private final Map<UUID, PosixServiceManager> managers = new LinkedHashMap<>();
    private final ServiceOperator operator = new Operator();

    private final Path engineTempPath;
    private final PosixServiceIOHandler handler;

    private volatile State state = State.CREATED;

    public final class Operator implements ServiceOperator {

        @Override
        public <S extends Service> S deploy(ServiceDefinition<S> serviceDefinition) throws ServiceException {
            if (!(serviceDefinition instanceof PosixServiceDefinition posixServiceDefinition)) {
                throw new IllegalArgumentException("Unsupported definition type!");
            }
            if (state != State.STARTED) {
                throw new IllegalStateException("Service not started!");
            }
            if (managers.containsKey(serviceDefinition.getUUID())) {
                throw new IllegalStateException("Already has definition with UUID: " + posixServiceDefinition.getUUID());
            }
            PosixServiceManager manager = new PosixServiceManager(posixServiceDefinition);
            managers.put(serviceDefinition.getUUID(), manager);
            manager.ensureStarted(PosixServiceEngine.this);
            return serviceDefinition.getServiceType().cast(manager.getService());
        }

        @Override
        public ServiceConnection connect(ServiceEndpoint serviceEndpoint) throws ServiceException {
            if (!(serviceEndpoint instanceof PosixServiceEndpoint posixServiceEndpoint)) {
                throw new IllegalArgumentException();
            }
            for (PosixServiceManager manager : managers.values()) {
                if (Objects.equals(manager.getService().getEndpoint().getPid(), posixServiceEndpoint.getPid())) {
                    return manager.connect();
                }
            }
            throw new ServiceException("No service found with pid: " + posixServiceEndpoint.getPid());
        }

        @Override
        public void retire(Identification serviceId) throws ServiceException {
            for (PosixServiceManager manager : managers.values()) {
                if (Objects.equals(manager.getService().getId(), serviceId)) {
                    manager.stop();
                }
            }
            throw new ServiceException("No service found with id: " + serviceId);
        }
    }

    public static PosixServiceEngine create() throws IOException {
        return new PosixServiceEngine(Files.createTempDirectory("svc-posix-"), PosixServiceIOHandler.create());
    }

    public PosixServiceEngine(Path engineTempPath, PosixServiceIOHandler handler) {
        this.engineTempPath = engineTempPath;
        this.handler = handler;
    }

    public Path getEngineTempPath() {
        return engineTempPath;
    }

    private void assertState(State expectedState) {
        if (state != expectedState) {
            throw new IllegalStateException();
        }
    }

    @Override
    public synchronized void init() throws ServiceException {
        try {
            if (!Files.exists(engineTempPath)) {
                Files.createDirectories(engineTempPath);
            }
            handler.init();
            assertState(State.CREATED);
            state = State.STOPPED;
            LOG.info("Engine initialized");
        } catch (IOException cause) {
            throw new ServiceException("Failed to create service directory: " + cause, cause);
        }
    }

    @Override
    public synchronized void start() throws ServiceException {
        assertState(State.STOPPED);
        for (PosixServiceManager manager : managers.values()) {
            manager.ensureStarted(this);
        }
        state = State.STARTED;
        LOG.info("Engine started");
    }

    @Override
    public void stop() throws ServiceException {
        assertState(State.STARTED);
        for (PosixServiceManager manager : managers.values()) {
            manager.ensureStopped();
        }
        state = State.STOPPED;
        LOG.info("Engine stopped");
    }

    @Override
    public void destroy() throws ServiceException {
        try {
            assertState(State.STOPPED);
            handler.shutdown();
            managers.clear();
            Files.walk(engineTempPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            state = State.DESTROYED;
            LOG.info("Engine destroyed");
        } catch (IOException cause) {
            throw new ServiceException("Failed to delete service directory: " + cause, cause);
        }
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public ServiceOperator getServiceOperator() {
        return operator;
    }

    @Override
    public List<? extends ServiceDefinition<?>> listServiceDefinitions() {
        List<PosixServiceDefinition> definitions = new ArrayList<>();
        for (PosixServiceManager manager : managers.values()) {
            definitions.add(manager.getDefinition());
        }
        return definitions;
    }

    @Override
    public List<? extends Service> listServices() {
        List<PosixService> services = new ArrayList<>();
        for (PosixServiceManager manager : managers.values()) {
            if (manager.isStarted()) {
                services.add(manager.getService());
            }
        }
        return services;
    }

    @Override
    public List<? extends ServiceConnection> listConnections() {
        List<PosixServiceConnection> connections = new ArrayList<>();
        for (PosixServiceManager manager : managers.values()) {
            for (PosixServiceConnection connection : manager.getConnections()) {
                if (!connection.isClosed()) {
                    connections.add(connection);
                }
            }
        }
        return connections;
    }

    @Override
    public List<Class<? extends Service>> getSupportedServiceTypes() {
        return List.of(PosixService.class);
    }

    public PosixServiceIOHandler getHandler() {
        return handler;
    }
}
