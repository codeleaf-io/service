package io.codeleaf.service.websocket;

import io.codeleaf.service.ServiceDefinition;

public interface WebSocketServiceDefinition extends ServiceDefinition<WebSocketService> {

    Class<?> getServerEndpointClass();

}
