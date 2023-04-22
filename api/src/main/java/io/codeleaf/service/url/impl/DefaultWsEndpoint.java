package io.codeleaf.service.url.impl;

import io.codeleaf.service.url.WsEndpoint;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.List;

public class DefaultWsEndpoint implements WsEndpoint {

    private final String virtualHost;
    private final String host;
    private final int portNumber;
    private final List<String> path;

    public DefaultWsEndpoint(String virtualHost, String host, int portNumber, List<String> path) {
        this.virtualHost = virtualHost;
        this.host = host;
        this.portNumber = portNumber;
        this.path = path;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public String getHost() {
        return host;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public List<String> getPath() {
        return path;
    }

    public static DefaultWsEndpoint create() throws IllegalStateException {
        return create("localhost", findFreePort());
    }

    private static int findFreePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException cause) {
            throw new IllegalStateException("No free port available: " + cause, cause);
        }
    }

    public static DefaultWsEndpoint create(String host, int portNumber) {
        return create(host, portNumber, null);
    }

    public static DefaultWsEndpoint create(String host, int portNumber, List<String> basePath) {
        return create(host, host, portNumber, basePath);
    }

    public static DefaultWsEndpoint create(String virtualHost, String host, int portNumber, List<String> basePath) {
        if (portNumber < 0) {
            throw new IllegalArgumentException();
        }
        if (virtualHost == null && host == null) {
            throw new IllegalArgumentException();
        }
        return new DefaultWsEndpoint(
                virtualHost != null ? virtualHost : host,
                host != null ? host : virtualHost,
                portNumber,
                basePath != null ? basePath : Collections.emptyList());
    }

}
