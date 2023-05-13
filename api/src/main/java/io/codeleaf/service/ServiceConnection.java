package io.codeleaf.service;

import java.io.Closeable;
import java.util.UUID;

public interface ServiceConnection extends Closeable {

    UUID getUUID();

    ServiceEngine getEngine();

    ServiceEndpoint getEndpoint();

    boolean isClosed();
}
