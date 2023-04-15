package io.codeleaf.service.engines.jaxrs;

import io.codeleaf.service.http.HttpEndpoint;
import org.apache.camel.builder.RouteBuilder;

import java.util.Objects;

public final class HttpListenRouteBuilder extends RouteBuilder {

    private final String ipAddress;
    private final int portNumber;
    private final EndpointRouter router;

    public HttpListenRouteBuilder(String ipAddress, int portNumber, EndpointRouter router) {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.router = router;
    }

    public String getRouteId() {
        return "listen_" + ipAddress + ":" + portNumber;
    }

    @Override
    public void configure() {
        from(String.format("jetty:http://%s:%s?matchOnUriPrefix=true&sendServerVersion=false&httpBindingRef=httpBinding", ipAddress, portNumber))
                .routeGroup("listen")
                .routeId(getRouteId())
                .bean(router);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof HttpListenRouteBuilder)) {
            return false;
        }
        HttpListenRouteBuilder other = (HttpListenRouteBuilder) object;
        return ipAddress.equals(other.ipAddress) && portNumber == other.portNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, portNumber);
    }

    public static HttpListenRouteBuilder create(HttpEndpoint serviceEndpoint, EndpointRouter router) {
        Objects.requireNonNull(serviceEndpoint);
        return new HttpListenRouteBuilder(serviceEndpoint.getHost(), serviceEndpoint.getPortNumber(), router);
    }

}
