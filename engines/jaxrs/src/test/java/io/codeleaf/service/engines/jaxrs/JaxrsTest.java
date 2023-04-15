package io.codeleaf.service.engines.jaxrs;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.common.behaviors.impl.DefaultIdentification;
import io.codeleaf.service.ServiceEndpoint;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.http.impl.DefaultHttpEndpoint;
import io.codeleaf.service.jaxrs.JaxrsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

        private final Set<Object> singletons = Collections.singleton(new MyJaxrsResource());

        public Set<Object> getSingletons() {
            return singletons;
        }

    }

    public static final class MyJaxrsService implements JaxrsService {

        private final Application application = new MyJaxrsApplication();

        @Override
        public Identification getId() {
            return new DefaultIdentification(() -> "myName", null, UUID.randomUUID());
        }

        @Override
        public List<ServiceEndpoint> getEndpoints() {
            return List.of(DefaultHttpEndpoint.create("localhost", 8181, Collections.singletonList("testPath")));
        }

        @Override
        public Application getApplication() {
            return application;
        }
    }

    private JaxrsServiceEngine engine;

    @BeforeEach
    public void before() throws ServiceException {
        engine = JaxrsServiceEngine.create();
        engine.init();
        engine.start();
    }

    @AfterEach
    public void after() throws ServiceException {
        engine.stop();
        engine.destroy();
        engine = null;
    }

    @Test
    public void test() throws ServiceException {
        // Given
        engine.getServiceOperator().deploy(new MyJaxrsService());

        // When
        String result = ClientBuilder.newClient().target("http://localhost:8181/testPath").path("hello").request().get(String.class);

        // Then
        Assertions.assertEquals("Hello World!", result);
    }

}
