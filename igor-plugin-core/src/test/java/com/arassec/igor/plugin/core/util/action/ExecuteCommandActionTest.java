package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link ExecuteCommandAction}.
 */
class ExecuteCommandActionTest {

    /**
     * Tests executing a command with the action on Windows systems.
     */
    @Test
    @DisplayName("Tests executing a command with the action on Windows systems.")
    @SuppressWarnings("unchecked")
    @EnabledOnOs({OS.WINDOWS})
    void testProcessWindows() {
        var action = new ExecuteCommandAction();

        action.setCommand("cmd.exe /c dir");

        List<Map<String, Object>> result = action.process(new HashMap<>(), new JobExecution());

        assertEquals(1, result.size());

        Map<String, Object> data = result.get(0);
        assertTrue(StringUtils.hasText(((String) ((Map<String, Object>) data.get("commandExecution")).get("stdout"))));
    }

    /**
     * Tests executing a command with the action on Unix systems.
     */
    @Test
    @DisplayName("Tests executing a command with the action on Unix systems.")
    @SuppressWarnings("unchecked")
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void testProcessUnix() {
        var action = new ExecuteCommandAction();

        action.setCommand("ls");

        List<Map<String, Object>> result = action.process(new HashMap<>(), new JobExecution());

        assertEquals(1, result.size());

        Map<String, Object> data = result.get(0);
        assertTrue(StringUtils.hasText(((String) ((Map<String, Object>) data.get("commandExecution")).get("stdout"))));
    }

}
