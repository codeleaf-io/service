package io.codeleaf.service;

import io.codeleaf.common.behaviors.Identification;

public interface Service {

    ServiceEngine getEngine();

    ServiceDefinition getDefinition();

    Identification getId();

    ServiceEndpoint getEndpoint();

}
