package com.arassec.igor.core.model.action.misc;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.service.persistence.PersistenceService;

import java.util.HashSet;
import java.util.Set;

@IgorAction(label = "Cleanup persisted values")
public class CleanupPersistedValuesAction implements Action {

    @IgorParam
    private PersistenceService persistenceService;

    @IgorParam
    private int numValuesToKeep;

    @Override
    public void initialize() {

    }

    @Override
    public boolean process(IgorData data) {
        return true;
    }

    @Override
    public boolean dryRun(IgorData data) {
        return true;
    }

    @Override
    public void complete(String jobId, String taskName) {
        persistenceService.cleanup(jobId, taskName, numValuesToKeep);
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public Set<String> provides() {
        return new HashSet<>();
    }

    @Override
    public Set<String> requires() {
        return new HashSet<>();
    }

}
