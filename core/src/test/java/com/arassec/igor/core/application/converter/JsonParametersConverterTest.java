package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.converter.model.DummyService;
import com.arassec.igor.core.application.converter.util.EncryptionUtil;
import com.arassec.igor.core.repository.ServiceRepository;
import com.github.openjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link JsonParametersConverter}.
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class JsonParametersConverterTest {

    /**
     * The converter under test.
     */
    @InjectMocks
    private JsonParametersConverter converter;

    /**
     * Repository for services. Required to get a service instance as a service parameter's value.
     */
    @Mock
    private ServiceRepository serviceRepository;

    /**
     * Utlity for encryption/decryption.
     */
    @Mock
    private EncryptionUtil encryptionUtil;

    /**
     * Tests conversion of simple parameters to JSON format.
     */
    @Test
    public void testConvertSimpleParametersToJson() {
        DummyService dummyService = new DummyService();
        dummyService.setIntParam(1);
        dummyService.setIntObjectParam(2);
        dummyService.setLongParam(3L);
        dummyService.setLongObjectParam(4L);
        dummyService.setBooleanParam(true);
        dummyService.setBooleanObjectParam(Boolean.TRUE);
        dummyService.setStringParam("string-parameter");
        dummyService.setSecuredParam("secured-parameter");
        dummyService.setOptionalParam("optional-parameter");

        JSONArray parameters = converter.convert(dummyService, false, true);

        assertEquals(1, parameters.getJSONObject(0).getInt(JsonKeys.VALUE));
        assertEquals(2, parameters.getJSONObject(1).getInt(JsonKeys.VALUE));
        assertEquals(3L, parameters.getJSONObject(2).getLong(JsonKeys.VALUE));
        assertEquals(4L, parameters.getJSONObject(3).getLong(JsonKeys.VALUE));
        assertEquals(true, parameters.getJSONObject(4).getBoolean(JsonKeys.VALUE));
        assertEquals(true, parameters.getJSONObject(5).getBoolean(JsonKeys.VALUE));
        assertEquals("string-parameter", parameters.getJSONObject(6).getString(JsonKeys.VALUE));
        assertEquals("secured-parameter", parameters.getJSONObject(7).getString(JsonKeys.VALUE));
        assertEquals("optional-parameter", parameters.getJSONObject(8).getString(JsonKeys.VALUE));
    }

}
