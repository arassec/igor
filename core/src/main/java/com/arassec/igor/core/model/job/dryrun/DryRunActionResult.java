package com.arassec.igor.core.model.job.dryrun;

import com.arassec.igor.core.model.provider.IgorData;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains the dry-run result of an action.
 */
@Data
public class DryRunActionResult {

    /**
     * The data after the action's processing.
     */
    private List<IgorData> results = new LinkedList<>();

}
