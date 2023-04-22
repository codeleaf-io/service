package io.codeleaf.service.jaxrs;

import io.codeleaf.service.Service;
import io.codeleaf.service.url.HttpEndpoint;

import javax.ws.rs.core.Application;

public interface JaxrsService extends Service {

    Application getApplication();

    HttpEndpoint getHttpEndpoint();
}
