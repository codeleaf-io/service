package io.codeleaf.service.url;

import io.codeleaf.service.ServiceEndpoint;

import java.net.URI;
import java.util.List;

public interface HttpEndpoint extends UrlEndpoint {

    String getVirtualHost();

    String getHost();

    int getPortNumber();

    List<String> getBasePath();

    default URI toURI() {
        return URI.create(String.format("http://%s%s", getVirtualHostString(), PathLists.toEncodedPath(getBasePath())));
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
