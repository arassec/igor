package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.persistence.test.TestConnector;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link Connector} mapping.
 */
@DisplayName("Tests mapping a connector.")
class ConnectorMapperTest extends MapperBaseTest {

    /**
     * Tests serializing a connector.
     */
    @Test
    @DisplayName("Tests serializing a connector.")
    @SneakyThrows(IOException.class)
    void testSerialization() {
        TestConnector testConnector = new TestConnector();
        testConnector.setId(TestConnector.CONNECTOR_ID);
        testConnector.setName("connector-name");
        testConnector.setIntParam(1);
        testConnector.setIntegerParam(2);
        testConnector.setLongParam(Long.MIN_VALUE);
        testConnector.setLongObjectParam(Long.MAX_VALUE);
        testConnector.setBooleanParam(Boolean.TRUE);
        testConnector.setBooleanObjectParam(Boolean.FALSE);
        testConnector.setStringParam("string-param");
        testConnector.setSecuredStringParam("secured-string-param");

        Writer serializedConnector = new StringWriter();

        connectorObjectMapper.writeValue(serializedConnector, testConnector);

        Files.writeString(Paths.get("target/connector-reference.json"), serializedConnector.toString());

        String serializedJson = serializedConnector.toString();
        String referenceJson = Files.readString(Paths.get("src/test/resources/connector-reference.json"));

        assertTrue(isJsonEqual(referenceJson, serializedJson));
    }

    /**
     * Tests deserializing a connector.
     */
    @Test
    @DisplayName("Tests deserializing a connector.")
    @SneakyThrows(IOException.class)
    void testDeserialization() {
        String connectorJson = Files.readString(Paths.get("src/test/resources/connector-reference.json"));

        TestConnector testConnector = (TestConnector) connectorObjectMapper.readValue(connectorJson, Connector.class);

        assertEquals(TestConnector.CONNECTOR_ID, testConnector.getId());
        assertEquals("connector-name", testConnector.getName());
        assertEquals(1, testConnector.getIntParam());
        assertEquals(2, testConnector.getIntegerParam());
        assertEquals(Long.MIN_VALUE, testConnector.getLongParam());
        assertEquals(Long.MAX_VALUE, testConnector.getLongObjectParam());
        assertEquals(Boolean.TRUE, testConnector.isBooleanParam());
        assertEquals(Boolean.FALSE, testConnector.getBooleanObjectParam());
        assertEquals("string-param", testConnector.getStringParam());
        assertEquals("TestSecurityProvider.decrypt()", testConnector.getSecuredStringParam());
    }

}
