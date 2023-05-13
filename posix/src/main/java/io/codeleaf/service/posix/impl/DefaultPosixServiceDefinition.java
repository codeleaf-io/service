package io.codeleaf.service.posix.impl;

import io.codeleaf.service.posix.PosixService;
import io.codeleaf.service.posix.PosixServiceDefinition;

import java.util.Objects;
import java.util.UUID;

public class DefaultPosixServiceDefinition implements PosixServiceDefinition {

    private final UUID uuid;
    private final String name;
    private final String[] command;

    public static DefaultPosixServiceDefinition create(String... command) {
        Objects.requireNonNull(command);
        if (command.length < 1) {
            throw new IllegalArgumentException();
        }
        return new DefaultPosixServiceDefinition(UUID.randomUUID(), command[0], command);
    }

    public DefaultPosixServiceDefinition(UUID uuid, String name, String[] command) {
        this.uuid = uuid;
        this.name = name;
        this.command = command;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<PosixService> getServiceType() {
        return PosixService.class;
    }

    @Override
    public String[] getCommand() {
        return command;
    }
}
