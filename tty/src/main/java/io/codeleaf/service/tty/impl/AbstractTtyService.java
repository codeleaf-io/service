package io.codeleaf.service.tty.impl;

import io.codeleaf.common.behaviors.Identification;
import io.codeleaf.service.ServiceEndpoint;
import io.codeleaf.service.tty.TtyEndpoint;
import io.codeleaf.service.tty.TtyService;

import java.io.IOException;
import java.util.List;

public abstract class AbstractTtyService implements TtyService {

    private final Identification id;
    private final List<TtyEndpoint> endpoints;

    public AbstractTtyService(Identification id) throws IOException {
        this.id = id;
        this.endpoints = List.of(PipedTtyEndpoint.create());
    }

    @Override
    public Identification getId() {
        return id;
    }

    @Override
    public List<? extends ServiceEndpoint> getEndpoints() {
        return endpoints;
    }

}
