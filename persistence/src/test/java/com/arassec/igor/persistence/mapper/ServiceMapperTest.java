package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.persistence.test.TestService;
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
 * Tests {@link Service} mapping.
 */
@DisplayName("Tests mapping a service.")
public class ServiceMapperTest extends MapperBaseTest {

    /**
     * Tests serializing a service.
     */
    @Test
    @DisplayName("Tests serializing a service.")
    @SneakyThrows(IOException.class)
    void testSerialization() {
        TestService testService = new TestService();
        testService.setId(TestService.SERVICE_ID);
        testService.setName("service-name");
        testService.setIntParam(1);
        testService.setIntegerParam(2);
        testService.setLongParam(Long.MIN_VALUE);
        testService.setLongObjectParam(Long.MAX_VALUE);
        testService.setBooleanParam(Boolean.TRUE);
        testService.setBooleanObjectParam(Boolean.FALSE);
        testService.setStringParam("string-param");
        testService.setSecuredStringParam("secured-string-param");

        Writer serializedService = new StringWriter();

        serviceObjectMapper.writeValue(serializedService, testService);

        Files.writeString(Paths.get("target/service-reference.json"), serializedService.toString());

        String serializedJson = serializedService.toString();
        String referenceJson = Files.readString(Paths.get("src/test/resources/service-reference.json"));

        assertTrue(isJsonEqual(referenceJson, serializedJson));
    }

    /**
     * Tests deserializing a service.
     */
    @Test
    @DisplayName("Tests deserializing a service.")
    @SneakyThrows(IOException.class)
    void testDeserialization() {
        String serviceJson = Files.readString(Paths.get("src/test/resources/service-reference.json"));

        TestService testService = (TestService) serviceObjectMapper.readValue(serviceJson, Service.class);

        assertEquals(TestService.SERVICE_ID, testService.getId());
        assertEquals(TestService.CATEGORY_ID, testService.getCategoryId());
        assertEquals(TestService.TYPE_ID, testService.getTypeId());
        assertEquals("service-name", testService.getName());
        assertEquals(1, testService.getIntParam());
        assertEquals(2, testService.getIntegerParam());
        assertEquals(Long.MIN_VALUE, testService.getLongParam());
        assertEquals(Long.MAX_VALUE, testService.getLongObjectParam());
        assertEquals(Boolean.TRUE, testService.isBooleanParam());
        assertEquals(Boolean.FALSE, testService.getBooleanObjectParam());
        assertEquals("string-param", testService.getStringParam());
        assertEquals("TestSecurityProvider.decrypt()", testService.getSecuredStringParam());
    }

}
