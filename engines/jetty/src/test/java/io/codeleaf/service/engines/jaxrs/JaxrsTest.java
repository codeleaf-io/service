package io.codeleaf.service.engines.jaxrs;

import io.codeleaf.service.Service;
import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.engines.jetty.JettyServiceEngine;
import io.codeleaf.service.jaxrs.JaxrsService;
import io.codeleaf.service.jaxrs.JaxrsServiceConnection;
import io.codeleaf.service.jaxrs.JaxrsServiceDefinition;
import io.codeleaf.service.jaxrs.impl.DefaultJaxrsServiceDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
    public void test() throws IOException, ServiceException {
        // Given
        JaxrsServiceDefinition definition = DefaultJaxrsServiceDefinition.create(new MyJaxrsApplication());
        try (ServiceEngine engine = JettyServiceEngine.createAndStart()) {
            JaxrsService service = engine.getServiceOperator().deploy(definition);

            try (JaxrsServiceConnection connection = service.connect()) {
                connection.open();

                // When
                String result = connection.getWebTarget().path("hello").request().get(String.class);

                // Then
                Assertions.assertEquals("Hello World!", result);
            } catch (Exception cause) {
                Assertions.fail();
            }
        }
    }

}
