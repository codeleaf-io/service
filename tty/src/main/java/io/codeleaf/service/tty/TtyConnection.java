package io.codeleaf.service.tty;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public interface TtyConnection extends AutoCloseable {

    InputStream getIn();

    PrintStream getOut();

    PrintStream getErr();

    @Override
    void close() throws IOException;

}
