package io.codeleaf.service.posix;

import io.codeleaf.service.ServiceEndpoint;

public interface PosixServiceEndpoint extends ServiceEndpoint {

    long getPid();

}
