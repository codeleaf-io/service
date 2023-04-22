package io.codeleaf.service.jaxrs;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.common.utils.IdentityBuilder;
import io.codeleaf.service.url.HttpEndpoint;
import io.codeleaf.service.url.impl.DefaultHttpEndpoint;

import javax.ws.rs.core.Application;
import java.util.Objects;

public class DefaultJaxrsService implements JaxrsService {

    private final Application application;
    private final Identification id;
    private final HttpEndpoint endpoint;

    public static DefaultJaxrsService create(Application application) {
        return create(application, DefaultHttpEndpoint.create());
    }

    public static DefaultJaxrsService create(Application application, HttpEndpoint httpEndpoint) {
        return create(application, new IdentityBuilder()
                .withName(httpEndpoint.getVirtualHostString() + "/" + httpEndpoint.getBasePath())
                .withURI(httpEndpoint.toURI())
                .build(), httpEndpoint);
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
    public Application getApplication() {
        return application;
    }

    @Override
    public HttpEndpoint getHttpEndpoint() {
        return endpoint;
    }
}
