package com.arassec.igor.web.model.simulation;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contains the simulation result of an action.
 */
@Data
public class SimulationActionResult {

    /**
     * The data after the action's processing.
     */
    private List<Map<String, Object>> results = new LinkedList<>();

    /**
     * Might contain an error cause if the action finished abnormally.
     */
    private String errorCause;

}
