package io.codeleaf.service.websocket;

import io.codeleaf.service.Service;
import io.codeleaf.service.url.WsEndpoint;

public interface WebSocketService extends Service {

    Class<?> getServerEndpointClass();

    WsEndpoint getWsEndpoint();
}
