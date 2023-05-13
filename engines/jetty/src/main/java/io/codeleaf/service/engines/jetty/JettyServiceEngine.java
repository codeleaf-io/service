package io.codeleaf.service.engines.jetty;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.service.*;
import io.codeleaf.service.jaxrs.JaxrsService;
import io.codeleaf.service.jaxrs.JaxrsServiceDefinition;
import io.codeleaf.service.jaxrs.impl.DefaultJaxrsService;
import io.codeleaf.service.jaxrs.impl.DefaultJaxrsServiceConnection;
import io.codeleaf.service.url.HttpEndpoint;
import io.codeleaf.service.url.WsEndpoint;
import io.codeleaf.service.websocket.WebSocketService;
import io.codeleaf.service.websocket.WebSocketServiceDefinition;
import io.codeleaf.service.websocket.impl.DefaultWebSocketService;
import io.codeleaf.service.websocket.impl.DefaultWebSocketServiceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public final class JettyServiceEngine implements ServiceEngine {

    private static final Logger LOG = LoggerFactory.getLogger(JettyServiceEngine.class);

    private final ServerManager serverManager;

    private final Map<UUID, ServiceDefinition<?>> definitions = new HashMap<>();
    private final Map<Identification, Service> services = new HashMap<>();
    private final Map<UUID, ServiceConnection> connections = new HashMap<>();
    private final Set<ServletEndpointHandler> handlers = new LinkedHashSet<>();
    private final ServiceOperator operator = new ServiceOperatorImpl();
    private volatile State state = State.CREATED;

    JettyServiceEngine(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    public static JettyServiceEngine create() {
        return new JettyServiceEngine(ServerManager.create());
    }

    public static JettyServiceEngine createAndStart() throws ServiceException {
        JettyServiceEngine jettyServiceEngine = new JettyServiceEngine(ServerManager.create());
        jettyServiceEngine.init();
        jettyServiceEngine.start();
        return jettyServiceEngine;
    }

    private void assertState(State expectedState) {
        if (state != expectedState) {
            throw new IllegalStateException();
        }
    }

    @Override
    public synchronized void init() throws ServiceException {
        assertState(State.CREATED);
        serverManager.initServer();
        state = State.STOPPED;
        LOG.info("Engine initialized");
    }

    @Override
    public synchronized void start() throws ServiceException {
        assertState(State.STOPPED);
        serverManager.startServer();
        state = State.STARTED;
        LOG.info("Engine started");
    }

    @Override
    public synchronized void stop() throws ServiceException {
        assertState(State.STARTED);
        serverManager.stopServer();
        state = State.STOPPED;
        LOG.info("Engine stopped");
    }

    @Override
    public synchronized void destroy() throws ServiceException {
        assertState(State.STOPPED);
        serverManager.shutdown();
        state = State.DESTROYED;
        LOG.info("Engine destroyed");
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
        return List.copyOf(definitions.values());
    }

    @Override
    public List<? extends Service> listServices() {
        return List.copyOf(services.values());
    }

    @Override
    public List<? extends ServiceConnection> listConnections() {
        return List.copyOf(connections.values());
    }

    @Override
    public List<Class<? extends Service>> getSupportedServiceTypes() {
        return List.of(JaxrsService.class, WebSocketService.class);
    }

    private synchronized void addServiceHandler(ServletEndpointHandler handler) throws ServiceException {
        Service service = handler.getService();
        Objects.requireNonNull(service);
        if (services.containsKey(service.getId())) {
            LOG.error("Service already present, this can't add: " + service.getId());
            throw new ServiceException("Service already present, this can't add: " + service.getId());
        }
        try {
            handlers.add(handler);
            services.put(service.getId(), service);
            serverManager.serviceAdded(service);
            LOG.info("Added service: " + service.getId());
        } catch (IOException cause) {
            throw new ServiceException("Failed to add service: " + cause, cause);
        }
    }

    private synchronized void removeService(Service service) throws ServiceException {
        if (!services.containsKey(service.getId())) {
            LOG.error("Service NOT present, thus can't remove: " + service.getId());
            throw new ServiceException("Service NOT present, thus can't remove: " + service.getId());
        }
        try {
            serverManager.serviceRemoved(service);
            handlers.removeIf(handler -> Objects.equals(handler.getService().getId(), service.getId()));
            services.remove(service.getId());
            LOG.info("Removed service: " + service.getId());
        } catch (IOException cause) {
            throw new ServiceException("Failed to add service: " + cause, cause);
        }
    }

    public final class ServiceOperatorImpl implements ServiceOperator {

        @Override
        public synchronized <S extends Service> S deploy(ServiceDefinition<S> serviceDefinition) throws ServiceException {
            if (state != State.STARTED) {
                throw new IllegalStateException();
            }
            Service service;
            if (definitions.containsKey(serviceDefinition.getUUID())) {
                throw new IllegalStateException("ServiceDefinition is already registered: " + serviceDefinition.getUUID());
            }
            if (serviceDefinition instanceof JaxrsServiceDefinition jaxrsServiceDefinition) {
                DefaultJaxrsService jaxrsService = DefaultJaxrsService.create(JettyServiceEngine.this, jaxrsServiceDefinition);
                addServiceHandler(JaxrsEndpointHandler.create(jaxrsService, serverManager.getHandler(jaxrsService)));
                service = jaxrsService;
            } else if (serviceDefinition instanceof WebSocketServiceDefinition webSocketServiceDefinition) {
                DefaultWebSocketService webSocketService = DefaultWebSocketService.create(JettyServiceEngine.this, webSocketServiceDefinition);
                addServiceHandler(WsEndpointHandler.create(webSocketService, serverManager.getHandler(webSocketService)));
                service = webSocketService;
            } else {
                throw new ServiceException("Unsupported service type: " + serviceDefinition.getClass());
            }
            definitions.put(serviceDefinition.getUUID(), serviceDefinition);
            return serviceDefinition.getServiceType().cast(service);
        }

        @Override
        public synchronized ServiceConnection connect(ServiceEndpoint serviceEndpoint) throws ServiceException {
            if (state != State.STARTED) {
                throw new IllegalStateException();
            }
            ServiceConnection connection;
            if (serviceEndpoint instanceof HttpEndpoint httpEndpoint) {
                connection = new DefaultJaxrsServiceConnection(UUID.randomUUID(), JettyServiceEngine.this, httpEndpoint);
                connections.put(connection.getUUID(), connection);
            } else if (serviceEndpoint instanceof WsEndpoint wsEndpoint) {
                connection = new DefaultWebSocketServiceConnection(UUID.randomUUID(), JettyServiceEngine.this, wsEndpoint);
                connections.put(connection.getUUID(), connection);
            } else {
                throw new ServiceException("Unsupported service type: " + serviceEndpoint.getClass());
            }
            return connection;
        }

        @Override
        public void retire(Identification serviceId) throws ServiceException {
            if (!services.containsKey(serviceId)) {
                LOG.error("Service NOT present, thus can't remove: " + serviceId);
                throw new ServiceException("Service NOT present, thus can't remove: " + serviceId);
            }
            removeService(services.get(serviceId));
        }

    }

}
