package io.codeleaf.service.engines.jaxrs;

import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.jaxrs.DefaultJaxrsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.util.Set;

public class JaxrsTest {

    static {
        System.setProperty("java.util.logging.config.file", JaxrsTest.class.getClassLoader().getResource("logging.properties").getFile());
    }

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
    public void test() throws IOException, InterruptedException {
        // Given
        DefaultJaxrsService jaxrsService = DefaultJaxrsService.create(new MyJaxrsApplication());
        try (ServiceEngine ignored = JaxrsServiceEngine.run(jaxrsService)) {

            // When
            String result = ClientBuilder.newClient().target(jaxrsService.getHttpEndpoint().toHostURI()).path("hello").request().get(String.class);

            // Then
            Assertions.assertEquals("Hello World!", result);
        }
    }

}
