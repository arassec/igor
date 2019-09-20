package com.arassec.igor.core.model.service;

import com.arassec.igor.core.model.IgorSimulationSafe;

/**
 * Defines a source of data that can be manipulated using actions, or a destination for data to store it in.
 */
public interface Service {

    /**
     * Returns the system-wide unique ID of the service.
     *
     * @return The ID.
     */
    @IgorSimulationSafe
    Long getId();

    /**
     * Sets the ID of the service.
     *
     * @param id The service's ID.
     */
    @IgorSimulationSafe
    void setId(Long id);

    /**
     * Returns the service's name.
     *
     * @return The service's name.
     */
    @IgorSimulationSafe
    String getName();

    /**
     * Sets the service's name.
     *
     * @param name The new service name to set.
     */
    @IgorSimulationSafe
    void setName(String name);

    /**
     * Tests the service's configuration.
     *
     * @throws ServiceException In case of errors.
     */
    void testConfiguration() throws ServiceException;

}
