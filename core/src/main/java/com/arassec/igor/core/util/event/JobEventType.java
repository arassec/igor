package com.arassec.igor.core.util.event;

/**
 * Defines different types of job events.
 */
public enum JobEventType {

    /**
     * A job has been saved.
     */
    SAVE,

    /**
     * A job has been deleted.
     */
    DELETE,

    /**
     * A job changed its state.
     */
    STATE_CHANGE,

    /**
     * A job's state should be refreshed.
     */
    STATE_REFRESH

}
