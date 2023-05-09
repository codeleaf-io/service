package io.codeleaf.service.jaxrs.impl;

import io.codeleaf.service.jaxrs.JaxrsService;
import io.codeleaf.service.jaxrs.JaxrsServiceDefinition;

import javax.ws.rs.core.Application;
import java.util.Objects;
import java.util.UUID;

public class DefaultJaxrsServiceDefinition implements JaxrsServiceDefinition {

    private final Application application;
    private final UUID uuid;
    private final String name;

    public static DefaultJaxrsServiceDefinition create(Application application) {
        return create(application, UUID.randomUUID());
    }

    public static DefaultJaxrsServiceDefinition create(Application application, UUID uuid) {
        return create(application, uuid, "jaxrs:" + uuid.toString());
    }

    public static DefaultJaxrsServiceDefinition create(Application application, String name) {
        return create(application, UUID.randomUUID(), name);
    }

    public static DefaultJaxrsServiceDefinition create(Application application, UUID uuid, String name) {
        Objects.requireNonNull(application);
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(name);
        return new DefaultJaxrsServiceDefinition(application, uuid, name);
    }

    public DefaultJaxrsServiceDefinition(Application application, UUID uuid, String name) {
        this.application = application;
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public Application getApplication() {
        return application;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<JaxrsService> getServiceType() {
        return JaxrsService.class;
    }
}
