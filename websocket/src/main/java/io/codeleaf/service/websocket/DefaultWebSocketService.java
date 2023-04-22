package io.codeleaf.service.websocket;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.common.utils.IdentityBuilder;
import io.codeleaf.service.url.WsEndpoint;
import io.codeleaf.service.url.impl.DefaultWsEndpoint;

import java.util.Objects;

public class DefaultWebSocketService implements WebSocketService {

    private final Class<?> serverEndpointClass;
    private final Identification id;
    private final WsEndpoint endpoint;

    public static DefaultWebSocketService create(Class<?> serverEndpointClass) {
        return create(serverEndpointClass, DefaultWsEndpoint.create());
    }

    public static DefaultWebSocketService create(Class<?> serverEndpointClass, WsEndpoint endpoint) {
        return create(serverEndpointClass, new IdentityBuilder()
                .withName(endpoint.getVirtualHost() + "/" + endpoint.getPath())
                .withURI(endpoint.toURI())
                .build(), endpoint);
    }


    public static DefaultWebSocketService create(Class<?> serverEndpointClass, Identification id, WsEndpoint endpoint) {
        Objects.requireNonNull(serverEndpointClass);
        Objects.requireNonNull(id);
        Objects.requireNonNull(id);
        return new DefaultWebSocketService(serverEndpointClass, id, endpoint);
    }

    public DefaultWebSocketService(Class<?> serverEndpointClass, Identification id, WsEndpoint endpoint) {
        this.serverEndpointClass = serverEndpointClass;
        this.id = id;
        this.endpoint = endpoint;
    }

    @Override
    public Class<?> getServerEndpointClass() {
        return serverEndpointClass;
    }

    @Override
    public Identification getId() {
        return id;
    }

    public WsEndpoint getWsEndpoint() {
        return endpoint;
    }
}
