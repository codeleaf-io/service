package io.codeleaf.service.engines.tty;

import io.codeleaf.common.behaviors.impl.DefaultIdentification;
import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.ServiceException;
import io.codeleaf.service.tty.TtyConnection;
import io.codeleaf.service.tty.TtyEndpoint;
import io.codeleaf.service.tty.impl.AbstractTtyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

public class TtyTest {

    public static class MyTtyService extends AbstractTtyService {

        public MyTtyService() throws IOException {
            super(new DefaultIdentification(() -> "MyTtyService", null, UUID.randomUUID()));
        }

        @Override
        public void run(TtyConnection connection) {
            connection.getOut().println("Hello World!");
        }

    }

    private ServiceEngine engine;

    @BeforeEach
    public void before() throws ServiceException {
        engine = new TtyServiceEngine();
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
    public void test() throws IOException, ServiceException {
        // Given
        engine.getServiceOperator().deploy(new MyTtyService());
        TtyEndpoint endpoint = (TtyEndpoint) engine.listServices().get(0).getEndpoints().get(0);
        Scanner in = new Scanner(endpoint.getStdout());

        // When
        String result = in.nextLine();

        // Then
        Assertions.assertEquals("Hello World!", result);
    }

}
