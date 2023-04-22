package io.codeleaf.service.engines.jetty;

import io.codeleaf.service.Service;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.websocket.WebSocketService;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.server.ServerEndpointConfig;

public final class WsEndpointHandler extends ServletEndpointHandler {

    private WsEndpointHandler(Service service) {
        super(service, null);
    }

    public static WsEndpointHandler create(WebSocketService service, ServletContextHandler handler) throws ServiceException {
        try {
            ServerContainer container = WebSocketServerContainerInitializer.initialize(handler);
            container.addEndpoint(ServerEndpointConfig.Builder.create(
                    service.getServerEndpointClass(),
                    service.getWsEndpoint().toURI().getPath()).build());
            return new WsEndpointHandler(service);
        } catch (Exception cause) {
            throw new ServiceException("Failed to initialize service handler: " + cause.getMessage(), cause);
        }
    }

}