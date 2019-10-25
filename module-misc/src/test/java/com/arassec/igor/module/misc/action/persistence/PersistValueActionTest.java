package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.PersistentValue;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.module.misc.action.BaseMiscActionTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link PersistValueAction}.
 */
@DisplayName("'Persist value' action tests.")
class PersistValueActionTest extends BaseMiscActionTest {

    /**
     * Tests processing the action with JSON-Path parameters.
     */
    @Test
    @DisplayName("Tests the action with JSON-Path parameters and without cleanup.")
    void testProcessWithoutCleanup() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);

        PersistValueAction action = new PersistValueAction();
        action.setInput("$.data." + PARAM_KEY);
        action.setNumValuesToKeep(0);

        // PowerMock doesn't support JUnit 5 yet...
        ReflectionTestUtils.setField(action, "persistentValueRepository", persistentValueRepositoryMock);

        ArgumentCaptor<PersistentValue> argCap = ArgumentCaptor.forClass(PersistentValue.class);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        assertEquals(1, result.size());
        verify(persistentValueRepositoryMock, times(1)).upsert(eq("1"), eq("1"), argCap.capture());
        assertEquals(PARAM_VALUE, argCap.getValue().getContent());
    }

}
