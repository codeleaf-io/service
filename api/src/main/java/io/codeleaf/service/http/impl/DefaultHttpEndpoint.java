package io.codeleaf.service.http.impl;

import io.codeleaf.service.http.HttpEndpoint;

import java.util.Collections;
import java.util.List;

public class DefaultHttpEndpoint implements HttpEndpoint {

    private final String virtualHost;
    private final String host;
    private final int portNumber;
    private final List<String> basePath;

    public DefaultHttpEndpoint(String virtualHost, String host, int portNumber, List<String> basePath) {
        this.virtualHost = virtualHost;
        this.host = host;
        this.portNumber = portNumber;
        this.basePath = basePath;
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

    public List<String> getBasePath() {
        return basePath;
    }

    public static DefaultHttpEndpoint create(String host, int portNumber) {
        return create(host, portNumber, null);
    }

    public static DefaultHttpEndpoint create(String host, int portNumber, List<String> basePath) {
        return create(host, host, portNumber, basePath);
    }

    public static DefaultHttpEndpoint create(String virtualHost, String host, int portNumber, List<String> basePath) {
        if (portNumber < 0) {
            throw new IllegalArgumentException();
        }
        if (virtualHost == null && host == null) {
            throw new IllegalArgumentException();
        }
        return new DefaultHttpEndpoint(
                virtualHost != null ? virtualHost : host,
                host != null ? host : virtualHost,
                portNumber,
                basePath != null ? basePath : Collections.emptyList());
    }

}
