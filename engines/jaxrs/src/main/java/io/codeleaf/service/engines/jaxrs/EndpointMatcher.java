package io.codeleaf.service.engines.jaxrs;

import io.codeleaf.service.http.HttpEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class EndpointMatcher {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointMatcher.class);

    private final Collection<EndpointHandler> handlers;

    public EndpointMatcher(Set<EndpointHandler> handlers) {
        this.handlers = handlers;
    }

    public EndpointHandler matchService(URI requestURI, List<String> requestPath) {
        EndpointHandler match = null;
        for (EndpointHandler handler : handlers) {
            LOG.debug("Going to match against: " + handler.getService().getId() + "@" + handler.getServiceEndpoint().toVirtualHostURI());
            if (endpointMatches(handler.getServiceEndpoint(), requestURI, requestPath)) {
                if (match == null || isBetterMatch(handler.getServiceEndpoint(), match.getServiceEndpoint())) {
                    match = handler;
                }
            }
        }
        return match;
    }

    private boolean endpointMatches(HttpEndpoint serviceEndpoint, URI requestURI, List<String> path) {
        if (!serviceEndpoint.getVirtualHost().equals(requestURI.getHost())) {
            LOG.debug("Hostname does not match: " + serviceEndpoint.getVirtualHost() + " vs " + requestURI.getHost());
            return false;
        }
        if (serviceEndpoint.getPortNumber() != requestURI.getPort()) {
            LOG.debug("Port number does not match: " + serviceEndpoint.getPortNumber() + " vs " + requestURI.getPort());
            return false;
        }
        if (!startsWith(serviceEndpoint.getBasePath(), path)) {
            LOG.debug("Path does not match: " + serviceEndpoint.getBasePath() + " vs " + path);
            return false;
        }
        return true;
    }

    private boolean isBetterMatch(HttpEndpoint serviceEndpoint, HttpEndpoint matchedEndpoint) {
        return serviceEndpoint.getBasePath().size() > matchedEndpoint.getBasePath().size();
    }

    private boolean startsWith(List<String> basePath, List<String> pathParts) {
        if (basePath.size() > pathParts.size()) {
            return false;
        }
        for (int i = 0; i < basePath.size(); i++) {
            if (!basePath.get(i).equals(pathParts.get(i))) {
                return false;
            }
        }
        return true;
    }

}
