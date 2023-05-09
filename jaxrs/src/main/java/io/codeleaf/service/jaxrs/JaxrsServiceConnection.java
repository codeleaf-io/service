package io.codeleaf.service.jaxrs;

import io.codeleaf.service.ServiceConnection;
import io.codeleaf.service.url.HttpEndpoint;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;

public interface JaxrsServiceConnection extends ServiceConnection {

    default void open() throws IOException {
        open(ClientBuilder.newClient());
    }

    void open(Client client) throws IOException;

    WebTarget getWebTarget();

    @Override
    HttpEndpoint getEndpoint();

}
