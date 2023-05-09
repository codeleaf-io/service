package io.codeleaf.service;

import io.codeleaf.common.behaviors.Identification;

public interface ServiceOperator {

    <S extends Service> S deploy(ServiceDefinition<S> serviceDefinition) throws ServiceException;

    ServiceConnection connect(ServiceEndpoint serviceEndpoint) throws ServiceException;

    void retire(Identification serviceId) throws ServiceException;

}
