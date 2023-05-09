package io.codeleaf.service.jaxrs;

import io.codeleaf.service.Service;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.url.HttpEndpoint;

public interface JaxrsService extends Service {

    @Override
    JaxrsServiceDefinition getDefinition();

    @Override
    HttpEndpoint getEndpoint();

    JaxrsServiceConnection connect() throws ServiceException;
}
