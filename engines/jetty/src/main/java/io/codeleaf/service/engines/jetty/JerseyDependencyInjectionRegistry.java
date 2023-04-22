package io.codeleaf.service.engines.jetty;

import io.codeleaf.service.DependencyInjectionRegistry;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import java.util.LinkedHashMap;
import java.util.Map;

public final class JerseyDependencyInjectionRegistry extends AbstractBinder implements DependencyInjectionRegistry {

    private final Map<Object, Class<?>> bindings = new LinkedHashMap<>();

    @Override
    public <T> JerseyDependencyInjectionRegistry register(T dependency, Class<? super T> typeClass) {
        bindings.put(dependency, typeClass);
        return this;
    }

    @Override
    protected void configure() {
        bindings.forEach((d, t) -> bind(d).to(t));
    }
}
