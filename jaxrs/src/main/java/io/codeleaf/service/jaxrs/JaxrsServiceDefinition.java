package io.codeleaf.service.jaxrs;

import io.codeleaf.service.ServiceDefinition;

import javax.ws.rs.core.Application;

public interface JaxrsServiceDefinition extends ServiceDefinition<JaxrsService> {

    Application getApplication();

}
