package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.core.CoreType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <h2>'Split String' Action</h2>
 *
 * <h3>Description</h3>
 * This action uses the split() method of Java strings to split an input string into a JSON array of sub-strings.
 *
 * <h3>Example</h3>
 * The following input:
 * <pre><code>a.simple.example.string</code></pre>
 * together with the regular expression:
 * <pre><code>\.</code></pre>
 * will result in the following data item:
 * <pre><code>
 * {
 *   "data":{
 *     "splitted":[
 *       "a",
 *       "simple",
 *       "example",
 *       "string"
 *     ]
 *   },
 *   "meta":{
 *     "jobId":"cd6e0b06-cdfd-4fc7-809d-644bf1f793f6",
 *     "timestamp":1673204387303
 *   }
 * }
 * </code></pre>
 */
@Getter
@Setter
@IgorComponent(categoryId = CoreCategory.UTIL, typeId = CoreType.SPLIT_STRING_ACTION)
public class SplitStringAction extends BaseAction {

    /**
     * The input string to split.
     */
    @NotBlank
    @IgorParam
    private String input;

    /**
     * A (Java) regular expression that is used to split the input string.
     */
    @NotBlank
    @IgorParam
    private String regex;

    /**
     * Key into the data item where the resulting array is put. Can be a dot-separated path.
     */
    @NotBlank
    @IgorParam(advanced = true)
    private String targetKey = "data.splitted";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        String inputResolved = CorePluginUtils.evaluateTemplate(data, input);
        String regexResolved = CorePluginUtils.evaluateTemplate(data, regex);
        String targetKeyResolved = CorePluginUtils.evaluateTemplate(data, targetKey);

        String[] result = inputResolved.split(regexResolved);

        CorePluginUtils.putValue(data, targetKeyResolved, Arrays.asList(result));

        return List.of(data);
    }

}
