package io.codeleaf.service.engines.tty;

import io.codeleaf.service.tty.TtyConnection;
import io.codeleaf.service.tty.TtyService;

public final class BridgeTtyService implements Runnable {

    private final TtyService service;
    private final TtyConnection connection;

    public BridgeTtyService(TtyService service, TtyConnection connection) {
        this.service = service;
        this.connection = connection;
    }

    @Override
    public void run() {
        service.run(connection);
    }

}
