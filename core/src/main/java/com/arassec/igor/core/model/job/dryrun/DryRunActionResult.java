package com.arassec.igor.core.model.job.dryrun;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contains the dry-run result of an action.
 */
@Data
public class DryRunActionResult {

    /**
     * The data after the action's processing.
     */
    private List<Map<String, Object>> results = new LinkedList<>();

}
