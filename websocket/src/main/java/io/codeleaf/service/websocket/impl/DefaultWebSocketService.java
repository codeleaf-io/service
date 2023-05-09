package io.codeleaf.service.websocket.impl;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.common.utils.IdentityBuilder;
import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.url.WsEndpoint;
import io.codeleaf.service.url.impl.DefaultWsEndpoint;
import io.codeleaf.service.websocket.WebSocketService;
import io.codeleaf.service.websocket.WebSocketServiceConnection;
import io.codeleaf.service.websocket.WebSocketServiceDefinition;

import java.util.Objects;

public class DefaultWebSocketService implements WebSocketService {

    private final ServiceEngine engine;
    private final WebSocketServiceDefinition definition;
    private final Identification id;
    private final WsEndpoint endpoint;

    public static DefaultWebSocketService create(ServiceEngine engine, WebSocketServiceDefinition definition) {
        return create(engine, definition, DefaultWsEndpoint.create());
    }

    public static DefaultWebSocketService create(ServiceEngine engine, WebSocketServiceDefinition definition, WsEndpoint endpoint) {
        return create(engine, definition, new IdentityBuilder()
                .withName(endpoint.getVirtualHost() + "/" + endpoint.getPath())
                .withURI(endpoint.toURI())
                .build(), endpoint);
    }


    public static DefaultWebSocketService create(ServiceEngine engine, WebSocketServiceDefinition definition, Identification id, WsEndpoint endpoint) {
        Objects.requireNonNull(definition);
        Objects.requireNonNull(id);
        Objects.requireNonNull(id);
        return new DefaultWebSocketService(engine, definition, id, endpoint);
    }

    public DefaultWebSocketService(ServiceEngine engine, WebSocketServiceDefinition definition, Identification id, WsEndpoint endpoint) {
        this.engine = engine;
        this.definition = definition;
        this.id = id;
        this.endpoint = endpoint;
    }

    @Override
    public ServiceEngine getEngine() {
        return null;
    }

    @Override
    public WebSocketServiceDefinition getDefinition() {
        return definition;
    }

    @Override
    public Identification getId() {
        return id;
    }

    @Override
    public WsEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public WebSocketServiceConnection connect() throws ServiceException {
        return (WebSocketServiceConnection) engine.getServiceOperator().connect(getEndpoint());
    }
}
