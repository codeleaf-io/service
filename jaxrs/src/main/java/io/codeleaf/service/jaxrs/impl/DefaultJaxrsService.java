package io.codeleaf.service.jaxrs.impl;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.common.utils.IdentityBuilder;
import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.jaxrs.JaxrsService;
import io.codeleaf.service.jaxrs.JaxrsServiceConnection;
import io.codeleaf.service.jaxrs.JaxrsServiceDefinition;
import io.codeleaf.service.url.HttpEndpoint;
import io.codeleaf.service.url.impl.DefaultHttpEndpoint;

import java.util.Objects;

public class DefaultJaxrsService implements JaxrsService {

    private final ServiceEngine engine;
    private final JaxrsServiceDefinition definition;
    private final Identification id;
    private final HttpEndpoint endpoint;

    public static DefaultJaxrsService create(ServiceEngine engine, JaxrsServiceDefinition definition) {
        return create(engine, definition, DefaultHttpEndpoint.create());
    }

    public static DefaultJaxrsService create(ServiceEngine engine, JaxrsServiceDefinition definition, HttpEndpoint httpEndpoint) {
        return create(engine, definition, new IdentityBuilder().withName(httpEndpoint.getVirtualHostString() + "/" + httpEndpoint.getBasePath()).withURI(httpEndpoint.toURI()).build(), httpEndpoint);
    }

    public static DefaultJaxrsService create(ServiceEngine engine, JaxrsServiceDefinition definition, Identification id, HttpEndpoint endpoint) {
        Objects.requireNonNull(definition);
        Objects.requireNonNull(id);
        Objects.requireNonNull(endpoint);
        return new DefaultJaxrsService(engine, definition, id, endpoint);
    }

    public DefaultJaxrsService(ServiceEngine engine, JaxrsServiceDefinition definition, Identification id, HttpEndpoint endpoint) {
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
    public JaxrsServiceDefinition getDefinition() {
        return definition;
    }

    @Override
    public Identification getId() {
        return id;
    }

    @Override
    public HttpEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public JaxrsServiceConnection connect() throws ServiceException {
        return (JaxrsServiceConnection) engine.getServiceOperator().connect(getEndpoint());
    }
}
