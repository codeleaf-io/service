package io.codeleaf.service;

import java.util.UUID;

public interface ServiceDefinition<S extends Service> {

    UUID getUUID();

    String getName();

    Class<S> getServiceType();
}
