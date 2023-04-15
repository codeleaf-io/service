package io.codeleaf.service.http;

import io.codeleaf.service.ServiceEndpoint;

import java.net.URI;
import java.util.List;

public interface HttpEndpoint extends ServiceEndpoint {

    String getVirtualHost();

    String getHost();

    int getPortNumber();

    List<String> getBasePath();

    default URI toVirtualHostURI() {
        return URI.create("http://" + getVirtualHostString() + PathLists.toEncodedPath(getBasePath()));
    }

    default URI toHostURI() {
        return URI.create("http://" + getHostString() + PathLists.toEncodedPath(getBasePath()));
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
