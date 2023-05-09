package io.codeleaf.service.engines.jetty;

import io.codeleaf.service.Service;
import io.codeleaf.service.ServiceException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ServerManager {

    private final Map<String, Integer> counts = new HashMap<>();
    private final Map<String, ServerConnector> connectors = new HashMap<>();
    private final Map<String, ServletContextHandler> handlers = new HashMap<>();

    private final Server server;
    private final ContextHandlerCollection context;

    public static ServerManager create() {
        Server server = new Server();
        ContextHandlerCollection handlers = new ContextHandlerCollection();
        server.setHandler(handlers);
        return new ServerManager(server, handlers);
    }

    public ServerManager(Server server, ContextHandlerCollection context) {
        this.server = server;
        this.context = context;
    }

    public ServletContextHandler getHandler(Service service) {
        return handlers.computeIfAbsent(getKeyName(service), k -> {
            ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            handler.setContextPath("/*");
            context.addHandler(handler);
            return handler;
        });
    }

    public ContextHandlerCollection getContext() {
        return context;
    }

    public synchronized void serviceAdded(Service service) throws IOException {
        String key = getKeyName(service);
        int port = getPort(service);
        String host = getHost(service);
        if (!counts.containsKey(key)) {
            try {
                ServerConnector connector = new ServerConnector(server) {{
                    setPort(port);
                    setHost(host);
                }};
                connectors.put(key, connector);
                counts.put(key, 0);
                server.addConnector(connector);
                connector.open();
                connector.start();
            } catch (Exception cause) {
                throw new IOException("Failed to start service: " + cause, cause);
            }
        }
        counts.put(key, counts.get(key) + 1);
    }

    public synchronized void serviceRemoved(Service service) throws IOException {
        String key = getKeyName(service);
        if (!counts.containsKey(key)) {
            return;
        }
        int count = counts.get(key);
        if (count > 1) {
            counts.put(key, count - 1);
        } else {
            try {
                counts.remove(key);
                ServerConnector connector = connectors.remove(key);
                server.removeConnector(connector);
                ContextHandler remove = handlers.remove(key);
                context.removeHandler(remove);
                connector.stop();
                connector.close();
            } catch (Exception cause) {
                throw new IOException("Failed to stop service: " + cause, cause);
            }
        }
    }

    private String getKeyName(Service service) {
        return "connector-" + getHost(service) + ":" + getPort(service);
    }

    private String getHost(Service service) {
        return service.getId().getURI().getHost();
    }

    private int getPort(Service service) {
        return service.getId().getURI().getPort();
    }

    public synchronized void initServer() throws ServiceException {
    }

    public synchronized void startServer() throws ServiceException {
        try {
            server.start();
            context.start();
        } catch (Exception cause) {
            throw new ServiceException("Failed to start the server: " + cause);
        }
    }

    public synchronized void stopServer() throws ServiceException {
        try {
            context.stop();
            for (Connector connector : server.getConnectors()) {
                connector.stop();
            }
            server.stop();
        } catch (Exception cause) {
            throw new ServiceException("Failed to stop the server: " + cause);
        }
    }

    public synchronized void shutdown() throws ServiceException {
        connectors.values().forEach(server::removeConnector);
        connectors.clear();
        counts.clear();
        handlers.clear();
    }
}
