package io.codeleaf.service.engines.jetty;

import io.codeleaf.service.Service;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.jaxrs.JaxrsService;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.Servlet;

public final class JaxrsEndpointHandler extends ServletEndpointHandler {

    private JaxrsEndpointHandler(Service service, Servlet servlet) {
        super(service, servlet);
    }

    public static JaxrsEndpointHandler create(JaxrsService service, ServletContextHandler handler) throws ServiceException {
        try {
            ServletContainer servlet = new ServletContainer(ResourceConfig.forApplication(service.getApplication()));
            ServletHolder servletHolder = new ServletHolder(servlet);
            handler.addServlet(servletHolder, service.getId().getURI().getPath() + "*");
            return new JaxrsEndpointHandler(service, servlet);
        } catch (Exception cause) {
            throw new ServiceException("Failed to initialize service handler: " + cause.getMessage(), cause);
        }
    }

}