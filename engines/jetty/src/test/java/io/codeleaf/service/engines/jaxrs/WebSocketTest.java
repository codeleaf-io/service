package io.codeleaf.service.engines.jaxrs;

import io.codeleaf.service.ServiceEngine;
import io.codeleaf.service.engines.jetty.JettyServiceEngine;
import io.codeleaf.service.websocket.impl.DefaultWebSocketService;
import io.codeleaf.service.websocket.WebSocketService;
import org.glassfish.tyrus.client.ClientManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.websocket.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class WebSocketTest {

    public static class MyEndpoint extends Endpoint {

        @Override
        public void onOpen(Session session, EndpointConfig endpointConfig) {
            try {
                System.out.println("$%^#%$^#$%^ STARTED #$%^#$^#$%");
                Thread.sleep(1_000);
                session.getBasicRemote().sendText("Hello World!");
                Thread.sleep(1_000);
                session.close();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @ClientEndpoint
    public static final class MyClient {

        @OnMessage
        public void onMessage(String message) {
            Assertions.assertEquals("Hello World!", message);
            System.out.println("@$%@%@#$%@ Got it: " + message);
            messageLatch.countDown();
        }
    }


    private static CountDownLatch messageLatch = new CountDownLatch(1);

    @Test
    public void test() throws IOException, DeploymentException, InterruptedException {
//        // Given
//        WebSocketService webSocketService = DefaultWebSocketService.create(MyEndpoint.class);
//
//        // When
//        try (ServiceEngine result = JettyServiceEngine.run(webSocketService)) {
//
//            // Then
//            Assertions.assertEquals(1, result.listServices().size());
//            Assertions.assertTrue(result.listServices().get(0) instanceof WebSocketService);
//            WebSocketService foundService = (WebSocketService) result.listServices().get(0);
//            Assertions.assertEquals(foundService.getId().getURI(), foundService.getWsEndpoint().toURI());
//            System.out.println("*** : " + foundService.getId().getURI().getPath());
//            ClientManager client = ClientManager.createClient();
//            try (Session session = client.connectToServer(MyClient.class, foundService.getWsEndpoint().toURI())) {
//                messageLatch.await();
//            }
//        }
    }

}
