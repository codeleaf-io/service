package io.codeleaf.service;

import io.codeleaf.common.behaviors.Identification;

public interface ServiceOperator {

    void deploy(Service service) throws ServiceException;

    void retire(Identification serviceId) throws ServiceException;

}
