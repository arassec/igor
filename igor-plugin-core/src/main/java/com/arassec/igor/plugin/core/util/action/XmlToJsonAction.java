package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.core.CoreType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <h2>'XML to JSON' Action</h2>
 *
 * <h3>Description</h3>
 * This action converts an XML string into a JSON object at the same location.
 *
 * <h3>Example</h3>
 * The following XML string:
 * <pre><code>
 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
 * &lt;element&gt;
 *     &lt;status&gt;400&lt;/status&gt;
 *     &lt;message&gt;This is an example.&lt;/message&gt;
 *     &lt;error&gt;A&lt;/error&gt;
 *     &lt;error&gt;B&lt;/error&gt;
 *     &lt;error&gt;C&lt;/error&gt;
 * &lt;/element&gt;
 * </code></pre>
 * will result in the following data item:
 * <pre><code>
 * {
 *   "data": {
 *     "convertedXml": {
 *       "status":"400",
 *       "message":"This is an example.",
 *       "error":[
 *         "A",
 *         "B",
 *         "C"
 *       ]
 *     }
 *   },
 *   "meta": {
 *     "jobId": "01d11f89-1b89-4fa0-8da4-cdd75229f8b5",
 *     "timestamp": 1599580925108
 *   }
 * }
 * </code></pre>
 */
@Getter
@Setter
@IgorComponent(categoryId = CoreCategory.UTIL, typeId = CoreType.XML_TO_JSON_ACTION)
public class XmlToJsonAction extends BaseUtilAction {

    /**
     * The XML string to convert. Can be a mustache template to read the value from an attribute of the processed data item.
     */
    @NotBlank
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String xml;

    /**
     * Key into the data item where the converted JSON is put. Can be a dot-separated path.
     */
    @NotBlank
    @IgorParam(advanced = true)
    private String targetKey = "data.convertedXml";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        String xmlResolved = CorePluginUtils.evaluateTemplate(data, xml);
        String targetKeyResolved = CorePluginUtils.evaluateTemplate(data, targetKey);

        try {
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode jsonNode = xmlMapper.readTree(xmlResolved.getBytes());

            ObjectMapper jsonMapper = new ObjectMapper();
            Map<String, Object> json = jsonMapper.convertValue(jsonNode, new TypeReference<>() {
            });

            CorePluginUtils.putValue(data, targetKeyResolved, json);
        } catch (IOException e) {
            throw new IgorException("Could not convert XML to JSON!", e);
        }

        return List.of(data);
    }

}
