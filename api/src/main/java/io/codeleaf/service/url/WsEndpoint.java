package io.codeleaf.service.url;

import io.codeleaf.service.ServiceEndpoint;

import java.net.URI;
import java.util.List;

public interface WsEndpoint extends ServiceEndpoint {

    String getVirtualHost();

    String getHost();

    int getPortNumber();

    List<String> getPath();

    default URI toURI() {
        return URI.create("ws://" + getVirtualHostString() + PathLists.toEncodedPath(getPath()));
    }

    default String getVirtualHostString() {
        return getPortNumber() == 80
                ? getVirtualHost()
                : getVirtualHost() + ":" + getPortNumber();
    }

    default String getHostString() {
        return getPortNumber() == 80
                ? getHost()
                : getHost() + ":" + getPortNumber();
    }
}
