package io.codeleaf.service.websocket.impl;

import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.url.WsEndpoint;
import io.codeleaf.service.websocket.WebSocketServiceConnection;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class DefaultWebSocketServiceConnection implements WebSocketServiceConnection {

    private volatile boolean closed;

    private Session session;

    private final UUID uuid;
    private final ServiceEngine engine;
    private final WsEndpoint endpoint;

    public static DefaultWebSocketServiceConnection create(ServiceEngine engine, WsEndpoint endpoint) {
        return create(UUID.randomUUID(), engine, endpoint);
    }

    public static DefaultWebSocketServiceConnection create(UUID uuid, ServiceEngine engine, WsEndpoint endpoint) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(endpoint);
        return new DefaultWebSocketServiceConnection(uuid, engine, endpoint);
    }

    public DefaultWebSocketServiceConnection(UUID uuid, ServiceEngine engine, WsEndpoint endpoint) {
        this.uuid = uuid;
        this.engine = engine;
        this.endpoint = endpoint;
    }

    @Override
    public synchronized void open(ClientManager clientManager, Class<?> endpointClass) throws IOException {
        Objects.requireNonNull(clientManager);
        Objects.requireNonNull(endpointClass);
        try {
            session = clientManager.asyncConnectToServer(endpointClass, endpoint.toURI()).get();
        } catch (DeploymentException | ExecutionException | InterruptedException cause) {
            throw new IOException("Failed to open connection: " + cause, cause);
        }
    }

    @Override
    public Session getSession() {
        return session;
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
    public WsEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            session.close();
        } finally {
            closed = true;
        }
    }
}
