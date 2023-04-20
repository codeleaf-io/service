package io.codeleaf.service.engines.jaxrs;

import io.codeleaf.service.ServiceException;
import io.codeleaf.service.http.HttpEndpoint;
import io.codeleaf.service.http.PathLists;
import io.codeleaf.service.jaxrs.JaxrsService;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public final class EndpointHandler implements Processor {

    private final JaxrsService service;
    private final HttpEndpoint serviceEndpoint;
    private final ServletContainer servlet;

    private EndpointHandler(JaxrsService service, HttpEndpoint serviceEndpoint, ServletContainer servlet) {
        this.service = service;
        this.serviceEndpoint = serviceEndpoint;
        this.servlet = servlet;
    }

    public static EndpointHandler create(JaxrsService service, HttpEndpoint serviceEndpoint) throws ServiceException {
        try {
            ServletContainer container = new ServletContainer(ResourceConfig.forApplication(service.getApplication()));
            container.init(new ServiceServletConfig(service, serviceEndpoint));
            return new EndpointHandler(service, serviceEndpoint, container);
        } catch (ServletException cause) {
            throw new ServiceException("Failed to initialize service handler: " + cause.getMessage(), cause);
        }
    }

    private static Enumeration<String> enumeration(String value) {
        return Collections.enumeration(Collections.singleton(value));
    }

    public JaxrsService getService() {
        return service;
    }

    public HttpEndpoint getServiceEndpoint() {
        return serviceEndpoint;
    }

    @Override
    public void process(Exchange exchange) throws ServletException, IOException {
        HttpServletRequest request = exchange.getIn().getHeader("CamelHttpServletRequest", HttpServletRequest.class);
        HttpServletResponse response = exchange.getIn().getHeader("CamelHttpServletResponse", HttpServletResponse.class);
        String contextPath = PathLists.toEncodedPath(serviceEndpoint.getBasePath());
        servlet.service(OperatorServletRequest.create(request, exchange.getIn(), contextPath), response);
    }

    private static final class OperatorServletRequest extends HttpServletRequestWrapper {

        private final Map<String, String> headers;
        private final String relativePath;
        private final String contextPath;

        private OperatorServletRequest(HttpServletRequest request, Map<String, String> headers, String relativePath, String contextPath) {
            super(request);
            this.headers = headers;
            this.relativePath = relativePath;
            this.contextPath = contextPath;
        }

        private static OperatorServletRequest create(HttpServletRequest request, Message in, String contextPath) {
            Map<String, String> operatorHeaders = new HashMap<>();
            operatorHeaders.put(OperatorCamelHeaders.SERVICE_ID, Objects.toString(in.getHeader(OperatorCamelHeaders.SERVICE_ID)));
            operatorHeaders.put(OperatorCamelHeaders.SERVICE_NAME, Objects.toString(in.getHeader(OperatorCamelHeaders.SERVICE_NAME)));
            operatorHeaders.put(OperatorCamelHeaders.ORIGINAL_URL, Objects.toString(in.getHeader(OperatorCamelHeaders.ORIGINAL_URL)));
            operatorHeaders.put(OperatorCamelHeaders.RELATIVE_PATH, Objects.toString(in.getHeader(OperatorCamelHeaders.RELATIVE_PATH)));
            return new OperatorServletRequest(request, operatorHeaders, operatorHeaders.get(OperatorCamelHeaders.RELATIVE_PATH), contextPath);
        }

        @Override
        public String getHeader(String name) {
            return headers.containsKey(name) ? headers.get(name) : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return headers.containsKey(name) ? enumeration(getHeader(name)) : super.getHeaders(name);
        }

        @Override
        public String getPathInfo() {
            return relativePath;
        }

        @Override
        public String getPathTranslated() {
            return relativePath; // TODO: should we translate?
        }

        @Override
        public String getContextPath() {
            return contextPath;
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Vector<String> headerNames = new Vector<>();
            Enumeration<String> enumeration = super.getHeaderNames();
            while (enumeration.hasMoreElements()) {
                headerNames.add(enumeration.nextElement());
            }
            headerNames.addAll(headers.keySet());
            return headerNames.elements();
        }
    }

    private static final class ServiceServletConfig implements ServletConfig {

        private final JaxrsService service;
        private final ServletContext context;

        ServiceServletConfig(JaxrsService service, HttpEndpoint serviceEndpoint) {
            this(service, new ContextHandler(PathLists.toEncodedPath(serviceEndpoint.getBasePath())).getServletContext());
        }

        ServiceServletConfig(JaxrsService service, ServletContext context) {
            this.service = service;
            this.context = context;
        }

        @Override
        public String getServletName() {
            return service.getId().getPrincipal().getName();
        }

        @Override
        public ServletContext getServletContext() {
            return context;
        }

        @Override
        public String getInitParameter(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return Collections.emptyEnumeration();
        }

    }

}