package com.arassec.igor.core.model.job.dryrun;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contains the dry-run results of a task.
 */
@Data
public class DryRunTaskResult {

    /**
     * Contains the provider's data.
     */
    private List<Map<String, Object>> providerResults = new LinkedList<>();

    /**
     * Contains the dry-run results of the task's actions.
     */
    private List<DryRunActionResult> actionResults = new LinkedList<>();

}
