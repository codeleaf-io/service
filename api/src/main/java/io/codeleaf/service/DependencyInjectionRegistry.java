package io.codeleaf.service;

import io.codeleaf.common.utils.SingletonServiceLoader;

public interface DependencyInjectionRegistry {

    static DependencyInjectionRegistry getInstance() {
        return SingletonServiceLoader.load(DependencyInjectionRegistry.class);
    }

    <T> DependencyInjectionRegistry register(T dependency, Class<? super T> lookupClass);

}
