package com.arassec.igor.plugin.data.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.data.DataType;
import com.arassec.igor.plugin.data.connector.JdbcDatasourceConnector;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <h2>'Query Data' Action</h2>
 *
 * <h3>Description</h3>
 * Queries a database and adds the result to the processed data item.
 *
 * <h3>Example</h3>
 * The following SQL query:
 * <pre><code>
 * SELECT subject AS sub, body AS bod FROM mail_log;
 * </code></pre>
 * might result in the following data items:
 * <pre><code>
 * {
 *   "queryResult": [
 *     {
 *       "sub": "Igor Data Test 1",
 *       "bod": "Test body number 1."
 *     },
 *     {
 *       "sub": "Igor Data Test 2",
 *       "bod": "Test body number 2."
 *     }
 *   ]
 * }
 * </code></pre>
 */
@Getter
@Setter
@IgorComponent(categoryId = CoreCategory.DATA, typeId = DataType.QUERY_DATA_ACTION)
public class QueryDataAction extends BaseDataAction {

    /**
     * The datasource to query the data from.
     */
    @NotNull
    @IgorParam
    private JdbcDatasourceConnector datasourceConnector;

    /**
     * The SQL statement to use.
     */
    @NotBlank
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String sqlStatement;

    /**
     * The target key to store the query result in. Can be a dot-separated path into the JSON object.
     */
    @NotBlank
    @IgorParam(advanced = true)
    private String targetKey = DEFAULT_QUERY_RESULT_PATH;

    /**
     * The key into the data item to get an optional transaction ID. Can be a dot-separated path into the JSON object.
     */
    @IgorParam(advanced = true)
    private String transactionKey;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        var sqlQueryResolved = CorePluginUtils.evaluateTemplate(data, sqlStatement);
        var targetKeyResolved = CorePluginUtils.evaluateTemplate(data, targetKey);
        var transactionKeyResolved = CorePluginUtils.evaluateTemplate(data, transactionKey);

        CorePluginUtils.getValue(data, transactionKeyResolved, UUID.class)
            .ifPresentOrElse(transactionId -> data.put(targetKeyResolved, datasourceConnector.query(jobExecution, transactionId, sqlQueryResolved)),
                () -> data.put(targetKeyResolved, datasourceConnector.query(jobExecution, null, sqlQueryResolved)));

        return List.of(data);
    }

}
