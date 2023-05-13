package io.codeleaf.service.engines.posix;

import io.codeleaf.service.ServiceException;
import io.codeleaf.service.posix.PosixService;
import io.codeleaf.service.posix.PosixServiceConnection;
import io.codeleaf.service.posix.PosixServiceDefinition;
import io.codeleaf.service.posix.impl.DefaultPosixServiceDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PosixTest {

    @Test
    public void test() throws IOException, ServiceException {
        // Given
        PosixServiceDefinition definition = DefaultPosixServiceDefinition.create("echo", "abc");
        try (PosixServiceEngine engine = PosixServiceEngine.create()) {
            engine.init();
            engine.start();

            PosixService service = engine.getServiceOperator().deploy(definition);
            try (PosixServiceConnection connection = service.connect()) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                connection.getStdout().read(buffer);

                // When
                byte[] result = buffer.array();

                // Then
                Assertions.assertEquals("abc\n", new String(result));
                Assertions.assertTrue(connection.isTerminated());
            }
        }
    }
}
