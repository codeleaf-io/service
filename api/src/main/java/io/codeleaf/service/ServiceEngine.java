package io.codeleaf.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public interface ServiceEngine extends Closeable {

    void init() throws ServiceException;

    void start() throws ServiceException;

    void stop() throws ServiceException;

    void destroy() throws ServiceException;

    ServiceOperator getServiceOperator();

    List<? extends Service> listServices();

    List<Class<? extends Service>> getSupportedServiceTypes();

    default boolean isSupportedServiceType(Class<? extends Service> serviceType) {
        return getSupportedServiceTypes().contains(serviceType);
    }

    @Override
    default void close() throws IOException {
        try {
            stop();
            destroy();
        } catch (ServiceException cause) {
            throw new IOException("Failed to close service engine: " + cause, cause);
        }
    }
}
