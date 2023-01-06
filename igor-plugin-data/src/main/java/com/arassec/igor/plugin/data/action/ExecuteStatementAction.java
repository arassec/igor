package com.arassec.igor.plugin.data.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.data.connector.JdbcDatasourceConnector;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <h2>'Execute Statement' Action</h2>
 *
 * <h3>Description</h3>
 * Executes an SQL statement against the configured database.
 */
@Getter
@Setter
@IgorComponent(categoryId = CoreCategory.DATA, typeId = "execute-statement-action")
public class ExecuteStatementAction extends BaseDataAction {

    /**
     * The datasource use for statement execution.
     */
    @NotNull
    @IgorParam
    private JdbcDatasourceConnector datasourceConnector;

    /**
     * The SQL statement to execute.
     */
    @NotEmpty
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String sqlStatement;

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
        var evaluatedSqlStatement = CorePluginUtils.evaluateTemplate(data, sqlStatement);
        var evaluatedTransactionKey = CorePluginUtils.evaluateTemplate(data, transactionKey);

        CorePluginUtils.getValue(data, evaluatedTransactionKey, UUID.class)
            .ifPresentOrElse(transactionId -> datasourceConnector.execute(jobExecution, transactionId, evaluatedSqlStatement),
                () -> datasourceConnector.execute(jobExecution, null, evaluatedSqlStatement));

        return List.of(data);
    }

}
