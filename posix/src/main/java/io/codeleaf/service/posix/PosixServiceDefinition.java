package io.codeleaf.service.posix;

import io.codeleaf.service.ServiceDefinition;

public interface PosixServiceDefinition extends ServiceDefinition<PosixService> {

    String[] getCommand();

}
