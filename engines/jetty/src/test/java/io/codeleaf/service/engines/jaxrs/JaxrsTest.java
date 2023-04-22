package io.codeleaf.service.engines.jaxrs;

import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.engines.jetty.JettyServiceEngine;
import io.codeleaf.service.jaxrs.DefaultJaxrsService;
import io.codeleaf.service.jaxrs.JaxrsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.util.Set;

public class JaxrsTest {

    @Path("/hello")
    public static final class MyJaxrsResource {

        @GET
        public String sayHello() {
            return "Hello World!";
        }

    }

    public static final class MyJaxrsApplication extends Application {

        public Set<Class<?>> getClasses() {
            return Set.of(MyJaxrsResource.class);
        }
    }

    @Test
    public void test() throws IOException {
        // Given
        JaxrsService jaxrsService = DefaultJaxrsService.create(new MyJaxrsApplication());

        // When
        try (ServiceEngine result = JettyServiceEngine.run(jaxrsService)) {

            // Then
            Assertions.assertEquals(1, result.listServices().size());
            Assertions.assertTrue(result.listServices().get(0) instanceof JaxrsService);
            JaxrsService foundService = (JaxrsService) result.listServices().get(0);
            Assertions.assertEquals(foundService.getId().getURI(), foundService.getHttpEndpoint().toURI());
            String response = ClientBuilder.newClient().target(foundService.getId().getURI()).path("hello").request().get(String.class);
            Assertions.assertEquals("Hello World!", response);
        }
    }

}
