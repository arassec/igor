package com.arassec.igor.module.misc.provider;


import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorProvider;
import com.arassec.igor.core.model.misc.ParameterSubtype;
import com.arassec.igor.core.model.provider.BaseProvider;
import com.arassec.igor.core.model.provider.IgorData;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides fixed input to tasks.
 */
@IgorProvider(label = "Fixed input")
public class FixedInputProvider  extends BaseProvider implements UtilProvider {

    /**
     * The service to use for file listing.
     */
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String input;

    /**
     * Indicates whether the input should be split by newlines or not.
     */
    @IgorParam
    private boolean separateLines = true;

    /**
     * The key the files are listed under.
     */
    @IgorParam
    private String dataKey = "data";

    /**
     * Contains the separate lines from the input.
     */
    private List<String> inputParts = new LinkedList<>();

    /**
     * Maintains the index into the inputParts-list.
     */
    private int index;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Long jobId, String taskId) {
        super.initialize(jobId, taskId);
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
    public IgorData next() {
        IgorData igorData = new IgorData(getJobId(), getTaskId());
        igorData.put(dataKey, inputParts.get(index));
        index++;
        return igorData;
    }

}
