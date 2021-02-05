package com.arassec.igor.simulation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link SimulationConfiguration}.
 */
@Slf4j
@DisplayName("Configuration test for the 'simulation' module.")
class SimulationConfigurationTest {

    /**
     * Tests for the presence of required annotations.
     */
    @Test
    @DisplayName("Tests for the presence of required annotations.")
    void testRequiredAnnotations() {
        Annotation[] annotations = SimulationConfiguration.class.getAnnotations();

        List<String> annotationNames =
                Arrays.stream(annotations).sequential().map(annotation -> annotation.annotationType().getSimpleName()).collect(Collectors.toList());

        log.debug("Annotations of Simulation-Configuration: {}", annotationNames);

        // Required to run simulations in the background:
        assertTrue(annotationNames.contains("EnableAsync"));

        // Required to cleanup stale simulations regularly:
        assertTrue(annotationNames.contains("EnableScheduling"));
    }

}
