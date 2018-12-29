package com.arassec.igor.core.model.job.dryrun;

import com.arassec.igor.core.model.provider.IgorData;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains the dry-run results of a task.
 */
@Data
public class DryRunTaskResult {

    /**
     * Contains the provider's data.
     */
    private List<IgorData> providerResults = new LinkedList<>();

    /**
     * Contains the dry-run results of the task's actions.
     */
    private List<DryRunActionResult> actionResults = new LinkedList<>();

}
