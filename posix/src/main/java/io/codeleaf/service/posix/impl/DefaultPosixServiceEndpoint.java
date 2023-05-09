package io.codeleaf.service.posix.impl;

import io.codeleaf.service.posix.PosixServiceEndpoint;

public class DefaultPosixServiceEndpoint implements PosixServiceEndpoint {

    private final long pid;

    public DefaultPosixServiceEndpoint(long pid) {
        this.pid = pid;
    }

    public long getPid() {
        return pid;
    }
}
