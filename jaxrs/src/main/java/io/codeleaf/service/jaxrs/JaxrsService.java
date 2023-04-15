package io.codeleaf.service.jaxrs;

import io.codeleaf.service.Service;

import javax.ws.rs.core.Application;

public interface JaxrsService extends Service {

    Application getApplication();

}
