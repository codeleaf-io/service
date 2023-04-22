package io.codeleaf.service.engines.websocket;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.service.*;
import io.codeleaf.service.jaxrs.JaxrsService;
import io.codeleaf.service.url.HttpEndpoint;
import io.codeleaf.service.websocket.WebSocketService;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.glassfish.tyrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public final class WebSocketServiceEngine implements ServiceEngine {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketServiceEngine.class);

    private final CamelContext camelContext;
    private final Map<HttpListenRouteBuilder, Set<Identification>> listenRouteMapping = new HashMap<>();
    private final Map<Identification, WebSocketService> services = new HashMap<>();
    private final Set<EndpointHandler> handlers = new LinkedHashSet<>();
    private final EndpointRouter router = new EndpointRouter(new EndpointMatcher(handlers));
    private final ServiceOperator operator = new ServiceOperatorImpl();

    WebSocketServiceEngine(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public static WebSocketServiceEngine create() {
        return new WebSocketServiceEngine(new DefaultCamelContext());
    }

    public static WebSocketServiceEngine run(WebSocketService service) throws IOException {
        try {
            WebSocketServiceEngine engine = WebSocketServiceEngine.create();
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
        try {
            LOG.info("Engine initialized");
        } catch (Exception cause) {
            LOG.error("Failed to initialize engine: " + cause.getMessage(), cause);
            throw new ServiceException("Failed to initialize engine: " + cause.getMessage(), cause);
        }
    }

    @Override
    public void start() throws ServiceException {
        try {
            LOG.info("Engine started");
        } catch (Exception cause) {
            LOG.error("Failed to start engine: " + cause.getMessage(), cause);
            throw new ServiceException("Failed to start engine: " + cause.getMessage(), cause);
        }
    }

    @Override
    public void stop() throws ServiceException {
        try {
            LOG.info("Engine stopped");
        } catch (Exception cause) {
            LOG.error("Failed to stop engine: " + cause.getMessage(), cause);
            throw new ServiceException("Failed to stop engine: " + cause.getMessage(), cause);
        }
    }

    @Override
    public void destroy() throws ServiceException {
        try {
        } catch (Exception cause) {
            throw new ServiceException("Failed to destroy engine: " + cause.getMessage(), cause);
        }
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
        return List.of(WebSocketService.class);
    }

    private void addService(WebSocketService service) throws ServiceException {
        Objects.requireNonNull(service);
        if (services.containsKey(service.getId())) {
            LOG.error("Service already present, this can't add: " + service.getId());
            throw new ServiceException("Service already present, this can't add: " + service.getId());
        }
        service.get
        Server server = new Server();
        server.start();
        for (ServiceEndpoint serviceEndpoint : service.getEndpoints()) {
            if (serviceEndpoint instanceof HttpEndpoint httpEndpoint) {
                HttpListenRouteBuilder listenRoute = HttpListenRouteBuilder.create(httpEndpoint, router);
                Set<Identification> serviceIds = listenRouteMapping.computeIfAbsent(listenRoute, k -> new HashSet<>());
                if (serviceIds.isEmpty()) {
                    addRoute(listenRoute);
                }
                serviceIds.add(service.getId());
                EndpointHandler handler = EndpointHandler.create(service, httpEndpoint);
                handlers.add(handler);

            }
        }
        services.put(service.getId(), service);
        LOG.info("Added service: " + service.getId());
    }

    private void removeService(WebSocketService service) throws ServiceException {
        if (!services.containsKey(service.getId())) {
            LOG.error("Service NOT present, thus can't remove: " + service.getId());
            throw new ServiceException("Service NOT present, thus can't remove: " + service.getId());
        }
        for (ServiceEndpoint serviceEndpoint : service.getEndpoints()) {
            if (serviceEndpoint instanceof HttpEndpoint httpEndpoint) {
                HttpListenRouteBuilder listenRoute = HttpListenRouteBuilder.create(httpEndpoint, router);
                Set<Identification> applicationIdentifiers = listenRouteMapping.getOrDefault(listenRoute, Collections.emptySet());
                if (applicationIdentifiers.remove(service.getId()) && applicationIdentifiers.isEmpty()) {
                    removeRoute(listenRoute.getRouteId());
                }
            }
        }
        handlers.removeIf(handler -> Objects.equals(handler.getService().getId(), service.getId()));
        services.remove(service.getId());
        LOG.info("Removed service: " + service.getId());
    }

    public final class ServiceOperatorImpl implements ServiceOperator {

        @Override
        public void deploy(Service service) throws ServiceException {
            if (!(service instanceof WebSocketService)) {
                throw new ServiceException("Unsupported service type: " + service.getClass());
            }
            addService((WebSocketService) service);
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
