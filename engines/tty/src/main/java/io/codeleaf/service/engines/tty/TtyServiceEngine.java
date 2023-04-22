package io.codeleaf.service.engines.tty;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.service.Service;
import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.ServiceOperator;
import io.codeleaf.service.tty.TtyConnection;
import io.codeleaf.service.tty.TtyEndpoint;
import io.codeleaf.service.tty.TtyService;
import io.codeleaf.service.tty.impl.PipedTtyEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;

public final class TtyServiceEngine implements ServiceEngine {

    private static final Logger LOG = LoggerFactory.getLogger(TtyServiceEngine.class);

    private final Map<Identification, TtyService> services = new LinkedHashMap<>();
    private final Map<Identification, List<TtyEndpoint>> endpoints = new LinkedHashMap<>();
    private final ServiceOperator operator = new ServiceOperatorImpl();
    private final Executor executor = command -> new Thread(command).run();

    @Override
    public void init() throws ServiceException {
    }

    @Override
    public void start() throws ServiceException {
    }

    @Override
    public void stop() throws ServiceException {
    }

    @Override
    public void destroy() throws ServiceException {
        for (TtyService service : services.values()) {
            closeService(service);
        }
        services.clear();
        endpoints.clear();
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
        return List.of(TtyService.class);
    }

    private void addService(TtyService service) throws ServiceException {
        Objects.requireNonNull(service);
        if (services.containsKey(service.getId())) {
            LOG.error("Service already present, this can't add: " + service.getId());
            throw new ServiceException("Service already present, this can't add: " + service.getId());
        }
        TtyEndpoint ttyEndpoint = service.getTtyEndpoint();
        endpoints.computeIfAbsent(service.getId(), (id) -> new LinkedList<>()).add(ttyEndpoint);
        executor.execute(new BridgeTtyService(service, createConnection(ttyEndpoint)));
        services.put(service.getId(), service);
        LOG.info("Added service: " + service.getId());
    }

    private TtyConnection createConnection(TtyEndpoint ttyEndpoint) throws ServiceException {
        if (ttyEndpoint instanceof PipedTtyEndpoint) {
            return ((PipedTtyEndpoint) ttyEndpoint).getConnection();
        } else {
            throw new ServiceException("Unsupported ttyEndpoint: " + (ttyEndpoint == null ? "null" : ttyEndpoint.getClass()));
        }
    }

    private void removeService(TtyService service) throws ServiceException {
        if (!services.containsKey(service.getId())) {
            LOG.error("Service NOT present, thus can't remove: " + service.getId());
            throw new ServiceException("Service NOT present, thus can't remove: " + service.getId());
        }
        closeService(service);
        services.remove(service.getId());
        LOG.info("Removed service: " + service.getId());
    }

    private void closeService(TtyService service) throws ServiceException {
        try {
            for (TtyEndpoint endpoint : endpoints.getOrDefault(service.getId(), Collections.emptyList())) {
                endpoint.getStdin().close();
                endpoint.getStderr().close();
                endpoint.getStdout().close();
            }
        } catch (IOException cause) {
            throw new ServiceException("Failed to remove service: " + cause);
        }
    }

    public static TtyServiceEngine run(TtyService service) throws IOException {
        try {
            TtyServiceEngine engine = new TtyServiceEngine();
            engine.init();
            engine.getServiceOperator().deploy(service);
            engine.start();
            return engine;
        } catch (ServiceException cause) {
            throw new IOException("Failed to run service: " + cause, cause);
        }
    }

    public final class ServiceOperatorImpl implements ServiceOperator {

        @Override
        public void deploy(Service service) throws ServiceException {
            if (!(service instanceof TtyService)) {
                throw new ServiceException("Unsupported service type: " + service.getClass());
            }
            addService((TtyService) service);
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
