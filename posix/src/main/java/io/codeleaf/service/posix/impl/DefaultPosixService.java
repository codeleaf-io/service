package io.codeleaf.service.posix.impl;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.posix.PosixService;
import io.codeleaf.service.posix.PosixServiceConnection;
import io.codeleaf.service.posix.PosixServiceDefinition;
import io.codeleaf.service.posix.PosixServiceEndpoint;

public class DefaultPosixService implements PosixService {

    private final ServiceEngine engine;
    private final PosixServiceDefinition definition;
    private final Identification id;
    private final PosixServiceEndpoint endpoint;

    public DefaultPosixService(ServiceEngine engine, PosixServiceDefinition definition, Identification id, PosixServiceEndpoint endpoint) {
        this.engine = engine;
        this.definition = definition;
        this.id = id;
        this.endpoint = endpoint;
    }

    @Override
    public ServiceEngine getEngine() {
        return engine;
    }

    @Override
    public PosixServiceDefinition getDefinition() {
        return definition;
    }

    @Override
    public Identification getId() {
        return id;
    }

    @Override
    public PosixServiceEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public PosixServiceConnection connect() throws ServiceException {
        return (PosixServiceConnection) engine.getServiceOperator().connect(endpoint);
    }
}
