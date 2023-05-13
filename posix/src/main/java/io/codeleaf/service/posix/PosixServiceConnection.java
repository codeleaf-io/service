package io.codeleaf.service.posix;

import io.codeleaf.service.ServiceConnection;
import io.codeleaf.service.ServiceException;

import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public interface PosixServiceConnection extends ServiceConnection {

    boolean isTerminated();

    int getExitValue();

    void doInput(ByteBuffer buffer) throws ServiceException;

    Pipe.SourceChannel getStdout();

    Pipe.SourceChannel getStderr();

    void signal(int signal) throws ServiceException;

    @Override
    PosixServiceEndpoint getEndpoint();
}
