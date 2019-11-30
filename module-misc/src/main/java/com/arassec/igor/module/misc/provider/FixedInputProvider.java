package com.arassec.igor.module.misc.provider;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;

import javax.validation.constraints.NotBlank;
import java.util.*;

/**
 * Provides fixed input to tasks.
 */
@IgorComponent
public class FixedInputProvider extends BaseUtilProvider {

    /**
     * The key for the provided data.
     */
    private static final String INPUT_KEY = "input";

    /**
     * The service to use for file listing.
     */
    @NotBlank
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String input;

    /**
     * Indicates whether the input should be split by newlines or not.
     */
    @IgorParam
    private boolean separateLines = true;

    /**
     * Contains the separate lines from the input.
     */
    private List<String> inputParts = new LinkedList<>();

    /**
     * Maintains the index into the inputParts-list.
     */
    private int index;

    /**
     * Creates a new component instance.
     */
    public FixedInputProvider() {
        super("6b0cdf46-1412-43c0-8b8a-467ea3a60796");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String jobId, String taskId, JobExecution jobExecution) {
        super.initialize(jobId, taskId, jobExecution);
        index = 0;
        if (input != null && !input.isEmpty()) {
            if (separateLines) {
                inputParts = Arrays.asList(input.split("\n"));
            } else {
                inputParts.add(input);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return (inputParts.size() > index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> next() {
        Map<String, Object> item = new HashMap<>();
        item.put(INPUT_KEY, inputParts.get(index));
        index++;
        return item;
    }

}
