package com.arassec.igor.core.model.job.dryrun;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains the dry-run results of the job.
 */
@Data
public class DryRunJobResult {

    /**
     * The dry-run results of every task of the job.
     */
    private List<DryRunTaskResult> taskResults = new LinkedList<>();

}
