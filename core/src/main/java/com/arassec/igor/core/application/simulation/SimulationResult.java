package com.arassec.igor.core.application.simulation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contains the results of a simulated job execution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResult {

    /**
     * The simulation results as JSON objects.
     */
    private List<Map<String, Object>> results = new LinkedList<>();

    /**
     * Might contain an error cause if the job finished abnormally.
     */
    private String errorCause;

}
