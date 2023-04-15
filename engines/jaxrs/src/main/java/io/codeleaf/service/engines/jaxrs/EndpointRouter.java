package io.codeleaf.service.engines.jaxrs;

import io.codeleaf.service.http.HttpEndpoint;
import io.codeleaf.service.http.PathLists;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.http.common.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EndpointRouter implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointRouter.class);

    private final EndpointMatcher matcher;

    public EndpointRouter(EndpointMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        URI requestURI = extractRequestURI(exchange.getIn());
        List<String> requestPath = PathLists.fromEncodedPath(requestURI.getRawPath());
        EndpointHandler matched = matcher.matchService(requestURI, requestPath);
        if (matched != null) {
            processRequest(matched, requestPath, exchange);
        } else {
            handleNotFound(requestURI, exchange);
        }
    }

    private URI extractRequestURI(Message in) {
        Map<String, Object> headers = in.getHeaders();
        return URI.create("http://" + headers.get("Host") + headers.get("CamelHttpUri"));
    }

    private void processRequest(EndpointHandler matched, List<String> requestPath, Exchange exchange) throws Exception {
        LOG.debug("Application found: " + matched.getService().getId());
        setOperatorHeaders(matched, requestPath, exchange.getIn());
        matched.process(exchange);
    }

    private void handleNotFound(URI uri, Exchange exchange) throws IOException {
        LOG.debug("No application found at: " + uri);
        HttpServletResponse response = ((HttpMessage) exchange.getIn()).getResponse();
        response.sendError(404, "Not found: " + uri);
    }

    private void setOperatorHeaders(EndpointHandler matched, List<String> requestPath, Message in) {
        in.setHeader(OperatorCamelHeaders.SERVICE_ID, matched.getService().getId());
        in.setHeader(OperatorCamelHeaders.SERVICE_NAME, matched.getService().getId().getPrincipal().getName());
        in.setHeader(OperatorCamelHeaders.ORIGINAL_URL, constructOriginalURL(matched.getServiceEndpoint(), in));
        in.setHeader(OperatorCamelHeaders.RELATIVE_PATH, getRemainingPath(matched.getServiceEndpoint(), requestPath));
    }

    private String constructOriginalURL(HttpEndpoint serviceEndpoint, Message in) {
        return in.getHeader(Exchange.HTTP_URL, String.class)
                .replace(
                        "://" + serviceEndpoint.getHost(),
                        "://" + in.getHeader("Host", String.class).split(":")[0]);
    }

    private List<String> getRemainingPath(HttpEndpoint serviceEndpoint, List<String> requestPath) {
        List<String> remainingPath = new ArrayList<>();
        for (int i = serviceEndpoint.getBasePath().size(); i < requestPath.size(); i++) {
            remainingPath.add(requestPath.get(i));
        }
        return remainingPath;
    }

}
