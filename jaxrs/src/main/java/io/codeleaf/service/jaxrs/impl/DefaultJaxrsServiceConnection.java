package io.codeleaf.service.jaxrs.impl;

import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.jaxrs.JaxrsServiceConnection;
import io.codeleaf.service.url.HttpEndpoint;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class DefaultJaxrsServiceConnection implements JaxrsServiceConnection {

    private volatile boolean closed;

    private Client client;
    private WebTarget webTarget;

    private final UUID uuid;
    private final ServiceEngine engine;
    private final HttpEndpoint endpoint;

    public DefaultJaxrsServiceConnection(UUID uuid, ServiceEngine engine, HttpEndpoint endpoint) {
        this.uuid = uuid;
        this.engine = engine;
        this.endpoint = endpoint;
    }

    @Override
    public synchronized void open(Client client) {
        Objects.requireNonNull(client);
        this.client = client;
        this.webTarget = client.target(endpoint.toURI());
    }

    @Override
    public synchronized WebTarget getWebTarget() {
        return webTarget;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public ServiceEngine getEngine() {
        return engine;
    }

    @Override
    public HttpEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            client.close();
        } catch (RuntimeException cause) {
            throw new IOException("Failed to close connection: " + cause, cause);
        } finally {
            closed = true;
        }
    }
}
