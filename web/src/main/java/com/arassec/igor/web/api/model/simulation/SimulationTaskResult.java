package com.arassec.igor.web.api.model.simulation;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contains the simulation results of a task.
 */
@Data
public class SimulationTaskResult {

    /**
     * Contains the provider's data.
     */
    private List<Map<String, Object>> results = new LinkedList<>();

    /**
     * Might contain an error cause if the task finished abnormally.
     */
    private String errorCause;

    /**
     * Contains the simulation results of the task's actions.
     */
    private List<SimulationActionResult> actionResults = new LinkedList<>();


}
