package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.core.CoreType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>'Execute Command' Action</h2>
 *
 * <h3>Description</h3>
 * Executes a command on the command line. The command is executed with the same rights the igor server is running with.<br>
 * <p>
 * The standard output and error stream of the command execution will be available in the following data items under the
 * 'commandExecution' key.
 */
@Getter
@Setter
@IgorComponent(categoryId = CoreCategory.UTIL, typeId = CoreType.EXECUTE_COMMAND_ACTION)
public class ExecuteCommandAction extends BaseAction {

    /**
     * The command to execute.
     */
    @NotEmpty
    @IgorParam
    private String command;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        var resolvedCommand = CorePluginUtils.evaluateTemplate(data, command);

        Map<String, Object> commandExecution = new HashMap<>();

        try {
            var process = Runtime.getRuntime().exec(resolvedCommand.split(" "));

            commandExecution.put("stdout", new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            commandExecution.put("stderr", new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));

            data.put("commandExecution", commandExecution);

            return List.of(data);
        } catch (IOException e) {
            throw new IgorException("Could not execute command!", e);
        }
    }

}
