package io.codeleaf.service;

import java.util.Optional;
import java.util.ServiceLoader;

public final class ServiceEngineLoader {

    private ServiceEngineLoader() {
    }

    public static ServiceEngine load() {
        Optional<ServiceEngine> first = ServiceLoader.load(ServiceEngine.class).findFirst();
        if (first.isEmpty()) {
            throw new IllegalStateException("No ServiceEngine registered!");
        }
        return first.get();
    }
}
