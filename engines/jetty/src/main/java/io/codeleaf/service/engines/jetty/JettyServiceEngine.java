package io.codeleaf.service.engines.jetty;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.service.Service;
import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.ServiceOperator;
import io.codeleaf.service.jaxrs.JaxrsService;
import io.codeleaf.service.websocket.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public final class JettyServiceEngine implements ServiceEngine {

    private static final Logger LOG = LoggerFactory.getLogger(JettyServiceEngine.class);

    private final ServerManager serverManager;
    private final Map<Identification, Service> services = new HashMap<>();
    private final Set<ServletEndpointHandler> handlers = new LinkedHashSet<>();
    private final ServiceOperator operator = new ServiceOperatorImpl();

    JettyServiceEngine(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    public static JettyServiceEngine create() {
        return new JettyServiceEngine(ServerManager.create());
    }

    public static JettyServiceEngine run(JaxrsService service) throws IOException {
        return doRun(service);
    }

    public static JettyServiceEngine run(WebSocketService service) throws IOException {
        return doRun(service);
    }

    private static JettyServiceEngine doRun(Service service) throws IOException {
        try {
            JettyServiceEngine engine = JettyServiceEngine.create();
            engine.init();
            engine.getServiceOperator().deploy(service);
            engine.start();
            return engine;
        } catch (ServiceException cause) {
            throw new IOException("Failed to run service: " + cause, cause);
        }
    }

    @Override
    public void init() throws ServiceException {
        serverManager.initServer();
        LOG.info("Engine initialized");
    }

    @Override
    public void start() throws ServiceException {
        serverManager.startServer();
        LOG.info("Engine started");
    }

    @Override
    public void stop() throws ServiceException {
        serverManager.stopServer();
        LOG.info("Engine stopped");
    }

    @Override
    public void destroy() throws ServiceException {
        serverManager.shutdown();
    }

    @Override
    public ServiceOperator getServiceOperator() {
        return operator;
    }

    @Override
    public List<? extends Service> listServices() {
        return List.copyOf(services.values());
    }

    @Override
    public List<Class<? extends Service>> getSupportedServiceTypes() {
        return List.of(JaxrsService.class);
    }

    private synchronized void addServiceHandler(ServletEndpointHandler handler) throws ServiceException {
        Service service = handler.getService();
        Objects.requireNonNull(service);
        if (services.containsKey(service.getId())) {
            LOG.error("Service already present, this can't add: " + service.getId());
            throw new ServiceException("Service already present, this can't add: " + service.getId());
        }
        handlers.add(handler);
        services.put(service.getId(), service);
        serverManager.serviceAdded(service);
        LOG.info("Added service: " + service.getId());
    }

    private synchronized void removeService(Service service) throws ServiceException {
        if (!services.containsKey(service.getId())) {
            LOG.error("Service NOT present, thus can't remove: " + service.getId());
            throw new ServiceException("Service NOT present, thus can't remove: " + service.getId());
        }
        serverManager.serviceRemoved(service);
        handlers.removeIf(handler -> Objects.equals(handler.getService().getId(), service.getId()));
        services.remove(service.getId());
        LOG.info("Removed service: " + service.getId());
    }

    public final class ServiceOperatorImpl implements ServiceOperator {

        @Override
        public void deploy(Service service) throws ServiceException {
            if (service instanceof JaxrsService jaxrsService) {
                addServiceHandler(JaxrsEndpointHandler.create(jaxrsService, serverManager.getHandler(service)));
            } else if (service instanceof WebSocketService webSocketService) {
                addServiceHandler(WsEndpointHandler.create(webSocketService, serverManager.getHandler(service)));
            } else {
                throw new ServiceException("Unsupported service type: " + service.getClass());
            }
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
