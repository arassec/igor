package com.arassec.igor.web.model.simulation;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains the simulation results of the job.
 */
@Data
public class SimulationJobResult {

    /**
     * The simulation results of every task of the job.
     */
    private List<SimulationTaskResult> taskResults = new LinkedList<>();

    /**
     * Might contain an error cause if the job finished abnormally.
     */
    private String errorCause;

}
