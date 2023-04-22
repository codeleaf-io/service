package io.codeleaf.service.tty;

import io.codeleaf.service.Service;

public interface TtyService extends Service {

    void run(TtyConnection connection);

    TtyEndpoint getTtyEndpoint();
}
