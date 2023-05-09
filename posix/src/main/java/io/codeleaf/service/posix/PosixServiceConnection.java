package io.codeleaf.service.posix;

import io.codeleaf.service.ServiceConnection;
import io.codeleaf.service.ServiceException;

import java.io.InputStream;
import java.io.OutputStream;

public interface PosixServiceConnection extends ServiceConnection {

    boolean isTerminated();

    int getExitValue();

    OutputStream getStdin();

    InputStream getStdout();

    InputStream getStderr();

    void signal(int signal) throws ServiceException;

    @Override
    PosixServiceEndpoint getEndpoint();
}
