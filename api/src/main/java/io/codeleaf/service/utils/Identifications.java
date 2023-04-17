package io.codeleaf.service.utils;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.common.behaviors.impl.DefaultIdentification;

import java.util.Objects;
import java.util.UUID;

public final class Identifications {

    private Identifications() {
    }

    public static Identification create() {
        return create(UUID.randomUUID());
    }

    public static Identification create(String name) {
        return create(name, UUID.randomUUID());
    }

    public static Identification create(String name, UUID uuid) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(uuid);
        return new DefaultIdentification(name::toString, null, uuid);
    }

    public static Identification create(UUID uuid) {
        return create(uuid.toString(), uuid);
    }
}
