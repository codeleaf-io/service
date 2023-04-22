package io.codeleaf.service.url;

import io.codeleaf.service.ServiceEndpoint;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public interface UrlEndpoint extends ServiceEndpoint {

    URI toURI();

    default URL toURL() {
        try {
            return toURI().toURL();
        } catch (MalformedURLException cause) {
            throw new IllegalStateException("Failed to provide URL: " + cause, cause);
        }
    }
}
