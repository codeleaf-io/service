package io.codeleaf.service.websocket;

import io.codeleaf.service.Service;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.url.WsEndpoint;

public interface WebSocketService extends Service {

    @Override
    WebSocketServiceDefinition getDefinition();

    @Override
    WsEndpoint getEndpoint();

    WebSocketServiceConnection connect() throws ServiceException;

}
