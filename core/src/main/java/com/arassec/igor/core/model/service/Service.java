package com.arassec.igor.core.model.service;

/**
 * Defines a source of data that can be manipulated using actions, or a destination for data to store it in.
 */
public interface Service {

    /**
     * Returns the system-wide unique ID of the service.
     *
     * @return The ID.
     */
    Long getId();

    /**
     * Sets the ID of the service.
     *
     * @param id The service's ID.
     */
    void setId(Long id);

    /**
     * Returns the service's name.
     *
     * @return The service's name.
     */
    String getName();

    /**
     * Sets the service's name.
     *
     * @param name The new service name to set.
     */
    void setName(String name);

    /**
     * Tests the service's configuration.
     *
     * @throws ServiceException In case of errors.
     */
    void testConfiguration() throws ServiceException;

}
