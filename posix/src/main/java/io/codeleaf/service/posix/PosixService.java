package io.codeleaf.service.posix;

import io.codeleaf.service.Service;
import io.codeleaf.service.ServiceException;

public interface PosixService extends Service {

    @Override
    PosixServiceDefinition getDefinition();

    @Override
    PosixServiceEndpoint getEndpoint();

    PosixServiceConnection connect() throws ServiceException;

}
