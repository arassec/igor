package com.arassec.igor.core.application.converter.simulation;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Container for all data processed during a job simulation.
 */
@Data
public class SimulationDataCollector {

    /**
     * Helper index that points to the current Task during creation of the collector data structures.
     */
    private int curIndex = 0;

    /**
     * Contains the list of {@link ProviderProxy} instances, one for each task.
     */
    private List<ProviderProxy> providerProxies = new LinkedList<>();

    /**
     * Contains the list of {@link ActionProxy} instances, indexed by the task's number they belong to.
     */
    private Map<Integer, List<ActionProxy>> actionProxies = new HashMap<>();

}
