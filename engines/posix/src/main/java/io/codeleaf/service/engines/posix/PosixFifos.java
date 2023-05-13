package io.codeleaf.service.engines.posix;

import java.io.IOException;
import java.nio.file.Path;

public final class PosixFifos {

    private PosixFifos() {
    }

    public static Path createFifo(Path path, String fifoName) throws IOException {
        try {
            Process process = new ProcessBuilder()
                    .directory(path.toFile())
                    .command("mkfifo", "-m", "600", fifoName)
                    .start();
            if (process.waitFor() != 0) {
                throw new IllegalStateException("Error: " + new String(process.getErrorStream().readAllBytes()));
            }
            return path.resolve(fifoName);
        } catch (InterruptedException | IOException cause) {
            throw new IOException("Failed to create fifo: " + cause, cause);
        }
    }

}
