package com.arassec.igor.core.model.service;

/**
 * Defines a source of data that can be manipulated using actions, or a destination for data to store it in.
 * <p>
 * Created by sensen on 3/29/17.
 */
public interface Service {

    /**
     * Returns the system wide unique ID of the service.
     *
     * @return The ID.
     */
    String getId();

    /**
     * Sets the ID of the service.
     *
     * @param id The service's ID.
     */
    void setId(String id);

    /**
     * Tests the service's configuration.
     *
     * @throws ServiceException In case of errors.
     */
    void testConfiguration() throws ServiceException;

}
