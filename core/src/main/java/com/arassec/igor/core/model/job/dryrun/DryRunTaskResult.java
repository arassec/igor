package com.arassec.igor.core.model.job.dryrun;

import com.arassec.igor.core.model.provider.IgorData;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class DryRunTaskResult {

    private List<IgorData> providerResults = new LinkedList<>();

    private List<DryRunActionResult> actionResults = new LinkedList<>();

}
