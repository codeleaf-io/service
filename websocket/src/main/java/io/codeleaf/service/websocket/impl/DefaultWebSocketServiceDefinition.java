package io.codeleaf.service.websocket.impl;

import io.codeleaf.service.websocket.WebSocketService;
import io.codeleaf.service.websocket.WebSocketServiceDefinition;

import java.util.Objects;
import java.util.UUID;

public class DefaultWebSocketServiceDefinition implements WebSocketServiceDefinition {

    private final Class<?> serverEndpointClass;
    private final UUID uuid;
    private final String name;

    public static DefaultWebSocketServiceDefinition create(Class<?> serverEndpointClass) {
        return create(serverEndpointClass, UUID.randomUUID());
    }

    public static DefaultWebSocketServiceDefinition create(Class<?> serverEndpointClass, UUID uuid) {
        return create(serverEndpointClass, uuid, "ws:" + uuid);
    }

    public static DefaultWebSocketServiceDefinition create(Class<?> serverEndpointClass, String name) {
        return create(serverEndpointClass, UUID.randomUUID(), name);
    }

    public static DefaultWebSocketServiceDefinition create(Class<?> serverEndpointClass, UUID uuid, String name) {
        Objects.requireNonNull(serverEndpointClass);
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(name);
        return new DefaultWebSocketServiceDefinition(serverEndpointClass, uuid, name);
    }

    public DefaultWebSocketServiceDefinition(Class<?> serverEndpointClass, UUID uuid, String name) {
        this.serverEndpointClass = serverEndpointClass;
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public Class<?> getServerEndpointClass() {
        return serverEndpointClass;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<WebSocketService> getServiceType() {
        return WebSocketService.class;
    }
}
