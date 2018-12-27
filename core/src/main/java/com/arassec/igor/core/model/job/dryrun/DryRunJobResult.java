package com.arassec.igor.core.model.job.dryrun;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class DryRunJobResult {

    private List<DryRunTaskResult> taskResults = new LinkedList<>();

}
