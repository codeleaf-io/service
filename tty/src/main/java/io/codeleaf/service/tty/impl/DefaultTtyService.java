package io.codeleaf.service.tty.impl;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.service.ServiceEndpoint;
import io.codeleaf.service.tty.TtyConnection;
import io.codeleaf.service.tty.TtyEndpoint;
import io.codeleaf.service.tty.TtyService;
import io.codeleaf.service.utils.Identifications;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
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
    public List<? extends ServiceEndpoint> getEndpoints() {
        return List.of(endpoint);
    }

    public OutputStream getStdin() {
        return endpoint.getStdin();
    }

    public InputStream getStdout() {
        return endpoint.getStdout();
    }

    public InputStream getStderr() {
        return endpoint.getStdout();
    }

    @Override
    public void run(TtyConnection connection) {
        service.accept(connection);
    }

    public static DefaultTtyService create(Consumer<TtyConnection> service) throws IOException {
        return create(Identifications.create(), service);
    }

    public static DefaultTtyService create(Identification id, Consumer<TtyConnection> service) throws IOException {
        Objects.requireNonNull(id);
        Objects.requireNonNull(service);
        return new DefaultTtyService(id, PipedTtyEndpoint.create(), service);
    }

}
