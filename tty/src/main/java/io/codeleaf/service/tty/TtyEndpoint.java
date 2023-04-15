package io.codeleaf.service.tty;

import io.codeleaf.service.ServiceEndpoint;

import java.io.InputStream;
import java.io.PrintStream;

public interface TtyEndpoint extends ServiceEndpoint, AutoCloseable {

    PrintStream getStdin();

    InputStream getStdout();

    InputStream getStderr();

}
