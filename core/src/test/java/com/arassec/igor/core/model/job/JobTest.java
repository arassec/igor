package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link Job} model.
 */
@DisplayName("Job Tests")
public class JobTest {

    /**
     * Tests running an empty job.
     */
    @Test
    @DisplayName("Tests running an empty job.")
    void testRunEmptyJob() {
        Job job = new Job();
        JobExecution jobExecution = new JobExecution();
        job.run(jobExecution);
        assertEquals(JobExecutionState.FINISHED, jobExecution.getExecutionState());
    }

}
