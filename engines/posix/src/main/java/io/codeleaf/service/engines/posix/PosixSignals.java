package io.codeleaf.service.engines.posix;

import java.io.IOException;

public final class PosixSignals {

    public static final int SIGSTOP = 8;
    public static final int SIGCONT = 15;
    public static final int SIGTERM = 11;
    public static final int SIGKILL = 9;

    private PosixSignals() {
    }

    public static void suspend(long pid) {
        signal(SIGSTOP, pid);
    }

    public static void resume(long pid) {
        signal(SIGCONT, pid);
    }

    public static void terminate(long pid) {
        signal(SIGTERM, pid);
    }

    public static void kill(long pid) {
        signal(SIGKILL, pid);
    }

    public static synchronized void signal(int signal, long pid) {
        try {
            String[] cmdarray = new String[]{"kill", "-" + signal, String.valueOf(pid)};
            Runtime.getRuntime().exec(cmdarray);
        } catch (IOException cause) {
            throw new IllegalStateException("Failed to signal!");
        }
    }

}
