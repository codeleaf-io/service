package io.codeleaf.service.websocket;

import io.codeleaf.service.ServiceConnection;
import io.codeleaf.service.url.WsEndpoint;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.Session;
import java.io.IOException;

public interface WebSocketServiceConnection extends ServiceConnection {

    default void open(Class<?> endpointClass) throws IOException {
        open(ClientManager.createClient(), endpointClass);
    }

    void open(ClientManager clientManager, Class<?> endpointClass) throws IOException;

    Session getSession();

    @Override
    WsEndpoint getEndpoint();

}
