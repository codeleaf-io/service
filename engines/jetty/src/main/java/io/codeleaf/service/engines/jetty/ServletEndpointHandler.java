package io.codeleaf.service.engines.jetty;

import io.codeleaf.service.Service;

import javax.servlet.Servlet;
import java.net.URI;

public abstract class ServletEndpointHandler {

    private final Service service;
    private final Servlet servlet;

    public ServletEndpointHandler(Service service, Servlet servlet) {
        this.service = service;
        this.servlet = servlet;
    }

    public Service getService() {
        return service;
    }

    public URI getURI() {
        return service.getId().getURI();
    }

    public Servlet getServlet() {
        return servlet;
    }

}
