package io.codeleaf.service.tty.impl;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.common.utils.IdentityBuilder;
import io.codeleaf.service.tty.TtyConnection;
import io.codeleaf.service.tty.TtyEndpoint;
import io.codeleaf.service.tty.TtyService;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class DefaultTtyService implements TtyService {

    private final Identification id;
    private final TtyEndpoint endpoint;
    private final Consumer<TtyConnection> service;

    public DefaultTtyService(Identification id, TtyEndpoint endpoint, Consumer<TtyConnection> service) {
        this.id = id;
        this.endpoint = endpoint;
        this.service = service;
    }

    @Override
    public Identification getId() {
        return id;
    }

    @Override
    public void run(TtyConnection connection) {
        service.accept(connection);
    }

    @Override
    public TtyEndpoint getTtyEndpoint() {
        return endpoint;
    }

    public static DefaultTtyService create(Consumer<TtyConnection> service) throws IOException {
        UUID uuid = UUID.randomUUID();
        return create(new IdentityBuilder()
                .withName(uuid.toString())
                .withURI(URI.create("urn:tty:" + uuid))
                .withInstanceId(uuid)
                .build(), service);
    }

    public static DefaultTtyService create(Identification id, Consumer<TtyConnection> service) throws IOException {
        Objects.requireNonNull(id);
        Objects.requireNonNull(service);
        return new DefaultTtyService(id, PipedTtyEndpoint.create(), service);
    }

}
