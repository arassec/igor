package com.arassec.igor.plugin.data.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
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
 * <h2>'Commit Transaction' Action</h2>
 *
 * <h3>Description</h3>
 * Commits a previously started database transaction.
 */
@Getter
@Setter
@IgorComponent(categoryId = CoreCategory.DATA, typeId = DataType.COMMIT_TRANSACTION_ACTION)
public class CommitTransactionAction extends BaseDataAction {

    /**
     * The datasource to commit the transaction to.
     */
    @NotNull
    @IgorParam
    private JdbcDatasourceConnector datasourceConnector;

    /**
     * The key into the data item to get the transaction ID. Can be a dot-separated path into the JSON object.
     */
    @NotBlank
    @IgorParam(advanced = true)
    private String transactionKey = DEFAULT_TRANSACTION_ID_PATH;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        var transactionKeyResolved = CorePluginUtils.evaluateTemplate(data, transactionKey);

        CorePluginUtils.getValue(data, transactionKeyResolved, UUID.class)
            .ifPresent(transactionId -> datasourceConnector.commitTransaction(jobExecution, transactionId));

        CorePluginUtils.removeValue(data, transactionKeyResolved);

        return List.of(data);
    }

}
