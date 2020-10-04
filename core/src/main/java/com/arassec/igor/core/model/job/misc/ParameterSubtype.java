package com.arassec.igor.core.model.job.misc;

/**
 * Models sub-types of igor parameters. This can be used to further specify a parameter, additionally to its java type. The main
 * purpose is to aid the user by providing custom inputs in the frontend.
 */
public enum ParameterSubtype {

    /**
     * Default value, no subtype, just the plain old java type.
     */
    NONE,

    /**
     * A string parameter that contains a CRON expression. This triggers the frontend to display a CRON picker next to the input
     * field for the CRON expression.
     */
    CRON,

    /**
     * A string parameter that contains multiple lines instead of only one line. This triggers the frontend to create a text-area
     * as input field instead of a single line input.
     */
    MULTI_LINE,

}
