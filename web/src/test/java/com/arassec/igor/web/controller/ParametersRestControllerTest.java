package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.web.mapper.WebMapperKey;
import com.arassec.igor.web.test.*;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link ParametersRestController}.
 */
@DisplayName("Parameters-Controller Tests")
class ParametersRestControllerTest extends RestControllerBaseTest {

    /**
     * Tests retrieval of connector parameters.
     */
    @Test
    @DisplayName("Tests retrieval of connector parameters.")
    @SneakyThrows(Exception.class)
    void testGetConnectorParameters() {
        when(igorComponentRegistry.createConnectorInstance(eq("type-id"), isNull())).thenReturn(new TestConnector());

        MvcResult mvcResult = mockMvc.perform(get("/api/parameters/connector/type-id")).andExpect(status().isOk()).andReturn();

        List<Map<String, Object>> parameters = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(8, parameters.size());

        verifyParameter(parameters.get(0), "intParam", "int", 0);
        verifyParameter(parameters.get(1), "integerParam", "java.lang.Integer", null);
        verifyParameter(parameters.get(2), "longParam", "long", 0);
        verifyParameter(parameters.get(3), "longObjectParam", "java.lang.Long", null);
        verifyParameter(parameters.get(4), "booleanParam", "boolean", false);
        verifyParameter(parameters.get(5), "booleanObjectParam", "java.lang.Boolean", null);
        verifyParameter(parameters.get(6), "stringParam", "java.lang.String", null);

        assertEquals("securedStringParam", parameters.get(7).get(WebMapperKey.NAME.getKey()));
        assertEquals("java.lang.String", parameters.get(7).get(WebMapperKey.TYPE.getKey()));
        assertNull(parameters.get(7).get(WebMapperKey.VALUE.getKey()));
        assertEquals(true, parameters.get(7).get(WebMapperKey.SECURED.getKey()));
        assertEquals(false, parameters.get(7).get(WebMapperKey.ADVANCED.getKey()));
        assertEquals(false, parameters.get(7).get(WebMapperKey.CONNECTOR.getKey()));
        assertEquals(ParameterSubtype.NONE.name(), parameters.get(7).get(WebMapperKey.SUBTYPE.getKey()));
    }

    /**
     * Tests retrieval of action parameters.
     */
    @Test
    @DisplayName("Tests retrieval of action parameters.")
    @SneakyThrows(Exception.class)
    void testGetActionParameters() {
        when(igorComponentRegistry.createActionInstance(eq("type-id"), isNull())).thenReturn(new TestAction());
        when(igorComponentRegistry.getConnectorParameterCategoryAndType(eq(TestConnectorInterface.class))).thenReturn(
                Map.of("two", Set.of("three", "b"), "one", Set.of("four")));

        MvcResult mvcResult = mockMvc.perform(get("/api/parameters/action/type-id")).andExpect(status().isOk()).andReturn();

        List<Map<String, Object>> parameters = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(2, parameters.size());

        assertEquals("testConnector", parameters.get(0).get(WebMapperKey.NAME.getKey()));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categoryCandidates = (List<Map<String, Object>>) parameters.get(0).get(WebMapperKey.CATEGORY_CANDIDATES.getKey());
        assertEquals(2, categoryCandidates.size());
        assertEquals("one", categoryCandidates.get(0).get(WebMapperKey.KEY.getKey()));
        assertEquals("alpha", categoryCandidates.get(0).get(WebMapperKey.VALUE.getKey()));
        assertEquals("two", categoryCandidates.get(1).get(WebMapperKey.KEY.getKey()));
        assertEquals("beta", categoryCandidates.get(1).get(WebMapperKey.VALUE.getKey()));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> typeCandidates = (List<Map<String, Object>>) categoryCandidates.get(0).get(WebMapperKey.TYPE_CANDIDATES.getKey());
        assertEquals(1, typeCandidates.size());
        assertEquals("four", typeCandidates.get(0).get(WebMapperKey.KEY.getKey()));
        assertEquals("delta", typeCandidates.get(0).get(WebMapperKey.VALUE.getKey()));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> secondTypeCandidates =
                (List<Map<String, Object>>) categoryCandidates.get(1).get(WebMapperKey.TYPE_CANDIDATES.getKey());
        assertEquals(2, secondTypeCandidates.size());
        assertEquals("b", secondTypeCandidates.get(0).get(WebMapperKey.KEY.getKey()));
        assertEquals("b", secondTypeCandidates.get(0).get(WebMapperKey.VALUE.getKey()));
        assertEquals("three", secondTypeCandidates.get(1).get(WebMapperKey.KEY.getKey()));
        assertEquals("gamma", secondTypeCandidates.get(1).get(WebMapperKey.VALUE.getKey()));

        assertEquals(false, parameters.get(0).get(WebMapperKey.SECURED.getKey()));
        assertEquals(false, parameters.get(0).get(WebMapperKey.ADVANCED.getKey()));
        assertEquals(true, parameters.get(0).get(WebMapperKey.CONNECTOR.getKey()));
        assertEquals(ParameterSubtype.NONE.name(), parameters.get(0).get(WebMapperKey.SUBTYPE.getKey()));

        assertEquals("numThreads", parameters.get(1).get(WebMapperKey.NAME.getKey()));
        assertEquals("int", parameters.get(1).get(WebMapperKey.TYPE.getKey()));
        assertEquals(1, parameters.get(1).get(WebMapperKey.VALUE.getKey()));
        assertEquals(false, parameters.get(1).get(WebMapperKey.SECURED.getKey()));
        assertEquals(true, parameters.get(1).get(WebMapperKey.ADVANCED.getKey()));
        assertEquals(false, parameters.get(1).get(WebMapperKey.CONNECTOR.getKey()));
        assertEquals(true, parameters.get(1).get(WebMapperKey.REQUIRED.getKey()));
        assertEquals(ParameterSubtype.NONE.name(), parameters.get(1).get(WebMapperKey.SUBTYPE.getKey()));
    }

    /**
     * Tests retrieval of provider parameters.
     */
    @Test
    @DisplayName("Tests retrieval of provider parameters.")
    @SneakyThrows(Exception.class)
    void testGetProviderParameters() {
        when(igorComponentRegistry.createProviderInstance(eq("type-id"), isNull())).thenReturn(new TestProvider());

        MvcResult mvcResult = mockMvc.perform(get("/api/parameters/provider/type-id")).andExpect(status().isOk()).andReturn();

        List<Map<String, Object>> parameters = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(4, parameters.size());

        assertEquals("emptyStringParam", parameters.get(0).get(WebMapperKey.NAME.getKey()));
        assertEquals("java.lang.String", parameters.get(0).get(WebMapperKey.TYPE.getKey()));
        assertNull(parameters.get(0).get(WebMapperKey.VALUE.getKey()));
        assertEquals(false, parameters.get(0).get(WebMapperKey.SECURED.getKey()));
        assertEquals(false, parameters.get(0).get(WebMapperKey.ADVANCED.getKey()));
        assertEquals(false, parameters.get(0).get(WebMapperKey.CONNECTOR.getKey()));
        assertEquals(false, parameters.get(0).get(WebMapperKey.REQUIRED.getKey()));
        assertEquals(ParameterSubtype.CRON.name(), parameters.get(0).get(WebMapperKey.SUBTYPE.getKey()));

        verifyParameter(parameters.get(1), "nullInteger", "java.lang.Integer", null);

        assertEquals("validatedInteger", parameters.get(2).get(WebMapperKey.NAME.getKey()));
        assertEquals("java.lang.Integer", parameters.get(2).get(WebMapperKey.TYPE.getKey()));
        assertNull(parameters.get(2).get(WebMapperKey.VALUE.getKey()));
        assertEquals(false, parameters.get(2).get(WebMapperKey.SECURED.getKey()));
        assertEquals(false, parameters.get(2).get(WebMapperKey.ADVANCED.getKey()));
        assertEquals(false, parameters.get(2).get(WebMapperKey.CONNECTOR.getKey()));
        assertEquals(true, parameters.get(2).get(WebMapperKey.REQUIRED.getKey()));
        assertEquals(ParameterSubtype.NONE.name(), parameters.get(2).get(WebMapperKey.SUBTYPE.getKey()));

        assertEquals("simulationLimit", parameters.get(3).get(WebMapperKey.NAME.getKey()));
        assertEquals("int", parameters.get(3).get(WebMapperKey.TYPE.getKey()));
        assertEquals(25, parameters.get(3).get(WebMapperKey.VALUE.getKey()));
        assertEquals(false, parameters.get(3).get(WebMapperKey.SECURED.getKey()));
        assertEquals(true, parameters.get(3).get(WebMapperKey.ADVANCED.getKey()));
        assertEquals(false, parameters.get(3).get(WebMapperKey.CONNECTOR.getKey()));
        assertEquals(true, parameters.get(3).get(WebMapperKey.REQUIRED.getKey()));
        assertEquals(ParameterSubtype.NONE.name(), parameters.get(3).get(WebMapperKey.SUBTYPE.getKey()));
    }

    /**
     * Tests retrieval of trigger parameters.
     */
    @Test
    @DisplayName("Tests retrieval of trigger parameters.")
    @SneakyThrows(Exception.class)
    void testGetTriggerParameters() {
        when(igorComponentRegistry.createTriggerInstance(eq("type-id"), isNull())).thenReturn(new TestTrigger());

        mockMvc.perform(get("/api/parameters/trigger/type-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("testParam"))
                .andExpect(jsonPath("$[0].value").value("666"));
    }

    /**
     * Verifies the supplied parameter against the supplied values.
     *
     * @param parameter The parameter to test.
     * @param name      The expected name.
     * @param type      The expected type.
     * @param value     The expected value.
     */
    private void verifyParameter(Map<String, Object> parameter, String name, String type, Object value) {
        assertEquals(name, parameter.get(WebMapperKey.NAME.getKey()));
        assertEquals(type, parameter.get(WebMapperKey.TYPE.getKey()));
        assertEquals(value, parameter.get(WebMapperKey.VALUE.getKey()));
        assertEquals(false, parameter.get(WebMapperKey.SECURED.getKey()));
        assertEquals(false, parameter.get(WebMapperKey.ADVANCED.getKey()));
        assertEquals(false, parameter.get(WebMapperKey.CONNECTOR.getKey()));
        assertEquals(ParameterSubtype.NONE.name(), parameter.get(WebMapperKey.SUBTYPE.getKey()));
    }

}