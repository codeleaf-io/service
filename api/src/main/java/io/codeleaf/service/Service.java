package io.codeleaf.service;

import io.codeleaf.common.behaviors.Identification;

import java.util.List;

public interface Service {

    Identification getId();

    List<? extends ServiceEndpoint> getEndpoints();

}
