package io.codeleaf.service.websocket.impl;

import io.codeleaf.service.url.WsEndpoint;
import io.codeleaf.service.websocket.WebSocketServiceConnection;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class DefaultWebSocketServiceConnection implements WebSocketServiceConnection {

    private Session session;

    private final UUID uuid;
    private final WsEndpoint endpoint;

    public static DefaultWebSocketServiceConnection create(WsEndpoint endpoint) {
        return create(UUID.randomUUID(), endpoint);
    }

    public static DefaultWebSocketServiceConnection create(UUID uuid, WsEndpoint endpoint) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(endpoint);
        return new DefaultWebSocketServiceConnection(uuid, endpoint);
    }

    public DefaultWebSocketServiceConnection(UUID uuid, WsEndpoint endpoint) {
        this.uuid = uuid;
        this.endpoint = endpoint;
    }

    @Override
    public synchronized void open(ClientManager clientManager, Class<?> endpointClass) throws IOException {
        Objects.requireNonNull(clientManager);
        Objects.requireNonNull(endpointClass);
        try {
            clientManager.asyncConnectToServer(endpointClass, endpoint.toURI());
        } catch (DeploymentException cause) {
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
    public WsEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public synchronized void close() throws IOException {
        session.close();
    }
}
