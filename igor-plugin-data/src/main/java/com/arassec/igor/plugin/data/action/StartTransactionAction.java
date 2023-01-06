package com.arassec.igor.plugin.data.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.data.connector.JdbcDatasourceConnector;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * <h2>'Start Transaction' Action</h2>
 *
 * <h3>Description</h3>
 * Starts a database transaction for <strong>each processed</strong> data item.
 *
 * <h3>Attention</h3>
 * Remember to commit the transaction with the 'Commit Transaction' action, or else it will be rolled back at the end
 * of the job execution!
 */
@Getter
@Setter
@IgorComponent(categoryId = CoreCategory.DATA, typeId = "start-transaction-action")
public class StartTransactionAction extends BaseDataAction {

    /**
     * The datasource to start the transaction in.
     */
    @NotNull
    @IgorParam
    private JdbcDatasourceConnector datasourceConnector;

    /**
     * The key into the data item to store the transaction ID in. Can be a dot-separated path into the JSON object.
     */
    @NotBlank
    @IgorParam(advanced = true)
    private String transactionKey = DEFAULT_TRANSACTION_ID_PATH;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        var evaluatedTransactionKey = CorePluginUtils.evaluateTemplate(data, transactionKey);

        CorePluginUtils.getValue(data, evaluatedTransactionKey, UUID.class).ifPresent(uuid -> {
            throw new IgorException("A transaction has already been opened under: " + evaluatedTransactionKey);
        });

        UUID transactionId = UUID.randomUUID();
        datasourceConnector.startTransaction(jobExecution, transactionId);

        CorePluginUtils.putValue(data, evaluatedTransactionKey, transactionId);

        return List.of(data);
    }

}
