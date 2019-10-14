package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link Job} model.
 */
@ExtendWith(MockitoExtension.class)
public class JobTest {

    /**
     * Tests running an empty job.
     */
    @Test
    void testRun() {
        Job job = new Job();
        JobExecution jobExecution = new JobExecution();
        job.run(jobExecution);
        assertEquals(JobExecutionState.FINISHED, jobExecution.getExecutionState());
    }

}
