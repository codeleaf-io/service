package io.codeleaf.service.engines.posix;

import io.codeleaf.service.*;

import java.util.List;

public final class PosixServiceEngine implements ServiceEngine {


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

    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public ServiceOperator getServiceOperator() {
        return null;
    }

    @Override
    public List<? extends ServiceDefinition> listServiceDefinitions() {
        return null;
    }

    @Override
    public List<? extends Service> listServices() {
        return null;
    }

    @Override
    public List<? extends ServiceConnection> listConnections() {
        return null;
    }

    @Override
    public List<Class<? extends Service>> getSupportedServiceTypes() {
        return null;
    }
}
