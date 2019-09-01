package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.converter.simulation.SimulationDataCollector;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConverterConfig {

    private boolean securityEnabled = false;

    private boolean annotationDataEnabled = false;

    private SimulationDataCollector simulationDataCollector;

    public ConverterConfig(boolean securityEnabled, boolean annotationDataEnabled) {
        this.securityEnabled = securityEnabled;
        this.annotationDataEnabled = annotationDataEnabled;
    }

}
