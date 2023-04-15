package io.codeleaf.service;

import java.util.List;

public interface ServiceEngine {

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

}
