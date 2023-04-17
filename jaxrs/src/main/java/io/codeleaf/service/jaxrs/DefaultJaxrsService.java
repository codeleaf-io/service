package io.codeleaf.service.jaxrs;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.service.ServiceEndpoint;
import io.codeleaf.service.http.HttpEndpoint;
import io.codeleaf.service.http.impl.DefaultHttpEndpoint;
import io.codeleaf.service.utils.Identifications;

import javax.ws.rs.core.Application;
import java.util.List;
import java.util.Objects;

public class DefaultJaxrsService implements JaxrsService {

    private final Application application;
    private final Identification id;
    private final HttpEndpoint endpoint;

    public static DefaultJaxrsService create(Application application) {
        return create(application, Identifications.create());
    }

    public static DefaultJaxrsService create(Application application, Identification id) {
        return create(application, id, DefaultHttpEndpoint.create());
    }

    public static DefaultJaxrsService create(Application application, HttpEndpoint endpoint) {
        return create(application, Identifications.create(), endpoint);
    }

    public static DefaultJaxrsService create(Application application, Identification id, HttpEndpoint endpoint) {
        Objects.requireNonNull(application);
        Objects.requireNonNull(id);
        Objects.requireNonNull(id);
        return new DefaultJaxrsService(application, id, endpoint);
    }

    public DefaultJaxrsService(Application application, Identification id, HttpEndpoint endpoint) {
        this.application = application;
        this.id = id;
        this.endpoint = endpoint;
    }

    @Override
    public Identification getId() {
        return id;
    }

    @Override
    public List<? extends ServiceEndpoint> getEndpoints() {
        return List.of(endpoint);
    }

    @Override
    public Application getApplication() {
        return application;
    }

    public HttpEndpoint getHttpEndpoint() {
        return endpoint;
    }
}
