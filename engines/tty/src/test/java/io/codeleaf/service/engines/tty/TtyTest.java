package io.codeleaf.service.engines.tty;

import io.codeleaf.service.tty.TtyService;
import io.codeleaf.service.tty.impl.DefaultTtyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Scanner;

public class TtyTest {

    @Test
    public void test() throws IOException {
        // Given
        TtyService service = DefaultTtyService.create(t -> t.getOut().println("Hello World!"));

        // When
        try (TtyServiceEngine result = TtyServiceEngine.run(service)) {

            // Then
            Assertions.assertEquals(1, result.listServices().size());
            Assertions.assertTrue(result.listServices().get(0) instanceof TtyService);
            Scanner in = new Scanner(((TtyService) result.listServices().get(0)).getTtyEndpoint().getStdout());
            Assertions.assertEquals("Hello World!", in.nextLine());
        }
    }

}
