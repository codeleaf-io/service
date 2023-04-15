package io.codeleaf.service.engines.jaxrs;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.service.*;
import io.codeleaf.service.http.HttpEndpoint;
import io.codeleaf.service.jaxrs.JaxrsService;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class JaxrsServiceEngine implements ServiceEngine {

    private static final Logger LOG = LoggerFactory.getLogger(JaxrsServiceEngine.class);

    private final CamelContext camelContext;
    private final Map<String, Object> beanRegistry;
    private final Map<HttpListenRouteBuilder, Set<Identification>> listenRouteMapping = new HashMap<>();
    private final Map<Identification, JaxrsService> services = new HashMap<>();
    private final Set<EndpointHandler> handlers = new LinkedHashSet<>();
    private final EndpointRouter router = new EndpointRouter(new EndpointMatcher(handlers));
    private final ServiceOperator operator = new ServiceOperatorImpl();

    JaxrsServiceEngine(CamelContext camelContext, Map<String, Object> beanRegistry) {
        this.camelContext = camelContext;
        this.beanRegistry = beanRegistry;
    }

    @Override
    public void init() throws ServiceException {
        try {
            beanRegistry.put("httpBinding", new HttpResponseAlreadyWrittenBinding());
            LOG.info("Engine initialized");
        } catch (Exception cause) {
            LOG.error("Failed to initialize engine: " + cause.getMessage(), cause);
            throw new ServiceException("Failed to initialize engine: " + cause.getMessage(), cause);
        }
    }

    @Override
    public void start() throws ServiceException {
        try {
            camelContext.start();
            LOG.info("Engine started");
        } catch (Exception cause) {
            LOG.error("Failed to start engine: " + cause.getMessage(), cause);
            throw new ServiceException("Failed to start engine: " + cause.getMessage(), cause);
        }
    }

    @Override
    public void stop() throws ServiceException {
        try {
            camelContext.stop();
            LOG.info("Engine stopped");
        } catch (Exception cause) {
            LOG.error("Failed to stop engine: " + cause.getMessage(), cause);
            throw new ServiceException("Failed to stop engine: " + cause.getMessage(), cause);
        }
    }

    @Override
    public void destroy() throws ServiceException {
        try {
            for (Route route : camelContext.getRoutes()) {
                camelContext.removeRoute(route.getId());
            }
            for (org.apache.camel.Endpoint endpoint : camelContext.getEndpoints()) {
                camelContext.removeEndpoint(endpoint);
            }
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
        return List.of(JaxrsService.class);
    }

    private void addRoute(RouteBuilder routeBuilder) throws ServiceException {
        try {
            camelContext.addRoutes(routeBuilder);
            LOG.info("Route added: " + routeBuilder);
        } catch (Exception cause) {
            LOG.error("Failed to add route: " + cause.getMessage(), cause);
            throw new ServiceException("Failed to add route: " + cause.getMessage(), cause);
        }
    }

    private void removeRoute(String routeId) throws ServiceException {
        try {
            camelContext.removeRoute(routeId);
            LOG.info("Route stopped: " + routeId);
            camelContext.removeRoute(routeId);
            LOG.info("Route removed: " + routeId);
        } catch (Exception cause) {
            LOG.error("Failed to remove route: " + cause.getMessage(), cause);
            throw new ServiceException("Failed to remove route: " + cause.getMessage(), cause);
        }
    }

    private void addService(JaxrsService service) throws ServiceException {
        Objects.requireNonNull(service);
        if (services.containsKey(service.getId())) {
            LOG.error("Service already present, this can't add: " + service.getId());
            throw new ServiceException("Service already present, this can't add: " + service.getId());
        }
        for (ServiceEndpoint serviceEndpoint : service.getEndpoints()) {
            if (serviceEndpoint instanceof HttpEndpoint) {
                HttpEndpoint httpEndpoint = (HttpEndpoint) serviceEndpoint;
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

    private void removeService(JaxrsService service) throws ServiceException {
        if (!services.containsKey(service.getId())) {
            LOG.error("Service NOT present, thus can't remove: " + service.getId());
            throw new ServiceException("Service NOT present, thus can't remove: " + service.getId());
        }
        for (ServiceEndpoint serviceEndpoint : service.getEndpoints()) {
            if (serviceEndpoint instanceof HttpEndpoint) {
                HttpEndpoint httpEndpoint = (HttpEndpoint) serviceEndpoint;
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

    public static JaxrsServiceEngine create() {
        SimpleRegistry beanRegistry = new SimpleRegistry();
        return new JaxrsServiceEngine(new DefaultCamelContext(beanRegistry), beanRegistry);
    }

    public final class ServiceOperatorImpl implements ServiceOperator {

        @Override
        public void deploy(Service service) throws ServiceException {
            if (!(service instanceof JaxrsService)) {
                throw new ServiceException("Unsupported service type: " + service.getClass());
            }
            addService((JaxrsService) service);
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
