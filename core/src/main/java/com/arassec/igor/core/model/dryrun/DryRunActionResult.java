package com.arassec.igor.core.model.dryrun;

import com.arassec.igor.core.model.provider.IgorData;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class DryRunActionResult {

    private List<IgorData> results = new LinkedList<>();

}
