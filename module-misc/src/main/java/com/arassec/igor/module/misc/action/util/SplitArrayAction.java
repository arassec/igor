package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Action that converts a JSON-Array into multiple data items with a single JSON-Object containing one of the old values from the
 * array.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class SplitArrayAction extends BaseUtilAction {

    /**
     * The JSON-Path query to the array element.
     */
    @NotEmpty
    @IgorParam
    private String jsonPathQuery;

    /**
     * Creates a new component instance.
     */
    public SplitArrayAction() {
        super("split-array-action");
    }

    /**
     * Converts a JSON-Array at the configured position and returns multiple data items from it. Each data item contains only a
     * single value from the array at the old array's position.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     *
     * @return Data items for each array element.
     */
    @SuppressWarnings({"rawtypes"})
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        List<Map<String, Object>> result = new LinkedList<>();

        DocumentContext documentContext = JsonPath.parse(data);

        Object queryResult = documentContext.read(jsonPathQuery);
        if (queryResult instanceof List) {
            for (Object element : (List) queryResult) {
                documentContext.set(jsonPathQuery, element);
                result.add(JsonPath.parse(documentContext.jsonString()).json());
            }
        }

        return result;
    }

}
