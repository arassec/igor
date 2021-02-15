package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.IgorComponent;

import java.util.Map;

/**
 * Defines the interfaces for a trigger, that triggers a job execution.
 */
public interface Trigger extends IgorComponent {

    /**
     * Creates the initial data item that is used as input for following actions.
     *
     * @return The initial data item.
     */
    Map<String, Object> createDataItem();

}
