package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.IgorComponent;

import java.util.Map;

/**
 * Defines the interfaces for a trigger, that triggers a job execution.
 */
public interface Trigger extends IgorComponent {

    /**
     * Returns meta-data from the trigger that is added to each data item.
     *
     * @return The trigger's meta-data.
     */
    Map<String, Object> getMetaData();

    /**
     * Returns data from the trigger that is added to each data item.
     *
     * @return Trigger-data.
     */
    Map<String, Object> getData();

}
