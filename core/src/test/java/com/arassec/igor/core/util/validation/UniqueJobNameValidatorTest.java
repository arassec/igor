package com.arassec.igor.core.util.validation;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.repository.JobRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link UniqueJobNameValidator}.
 */
@DisplayName("Unique-Job-Name-Validator tests")
public class UniqueJobNameValidatorTest {

    /**
     * Tests validation of a connector's name.
     */
    @Test
    @DisplayName("Tests validation of a connector's name.")
    void testIsValid() {
        JobRepository jobRepositoryMock = mock(JobRepository.class);

        UniqueJobNameValidator validator = new UniqueJobNameValidator(jobRepositoryMock);

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid(new Job(), null));

        Job job = Job.builder().name("job").id("id").build();
        Job existingJob = Job.builder().name("job").id("id").build();
        when(jobRepositoryMock.findByName(eq("job"))).thenReturn(existingJob);

        assertTrue(validator.isValid(job, null));

        existingJob = Job.builder().name("job").id("other-id").build();
        when(jobRepositoryMock.findByName(eq("job"))).thenReturn(existingJob);

        assertFalse(validator.isValid(job, null));
    }

}
