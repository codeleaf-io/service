package io.codeleaf.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public interface ServiceEngine extends Closeable {

    enum State {
        CREATED,
        STOPPED,
        STARTED,
        DESTROYED
    }

    void init() throws ServiceException;

    void start() throws ServiceException;

    void stop() throws ServiceException;

    void destroy() throws ServiceException;

    State getState();

    ServiceOperator getServiceOperator();

    List<? extends ServiceDefinition> listServiceDefinitions();

    List<? extends Service> listServices();

    List<? extends ServiceConnection> listConnections();

    List<Class<? extends Service>> getSupportedServiceTypes();

    default boolean isSupportedServiceType(Class<? extends Service> serviceType) {
        return getSupportedServiceTypes().contains(serviceType);
    }

    @Override
    default void close() throws IOException {
        try {
            if (getState() == State.CREATED) {
                return;
            }
            if (getState() == State.STARTED) {
                stop();
            }
            destroy();
        } catch (ServiceException cause) {
            throw new IOException("Failed to close service engine: " + cause, cause);
        }
    }
}
