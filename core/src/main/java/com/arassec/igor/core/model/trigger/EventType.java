package com.arassec.igor.core.model.trigger;

/**
 * Contains the types of events that can cause a trigger fire.
 */
public enum EventType {

    /**
     * This event has an unknown type.
     */
    NONE,

    /**
     * An incoming message is the source of the event.
     */
    MESSAGE,

    /**
     * A web request against a web-hook is the event's source.
     */
    WEB_HOOK,

}
