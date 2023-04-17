package io.codeleaf.service.engines.tty;

import io.codeleaf.service.tty.impl.DefaultTtyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class TtyTest {

    @Test
    public void test() throws IOException {
        // Given
        DefaultTtyService service = DefaultTtyService.create(t -> t.getOut().println("Hello World!"));
        try (TtyServiceEngine engine = TtyServiceEngine.run(service)) {
            InputStream stdout = ((DefaultTtyService) engine.listServices().get(0)).getStdout();
            Scanner in = new Scanner(stdout);

            // When
            String result = in.nextLine();

            // Then
            Assertions.assertEquals("Hello World!", result);
        }
    }

}
