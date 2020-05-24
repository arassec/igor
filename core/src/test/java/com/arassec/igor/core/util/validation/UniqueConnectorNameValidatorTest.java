package com.arassec.igor.core.util.validation;

import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.repository.ConnectorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link UniqueConnectorNameValidator}.
 */
@DisplayName("Unique-Connector-Name-Validator tests")
public class UniqueConnectorNameValidatorTest {

    /**
     * Tests validation of a connector's name.
     */
    @Test
    @DisplayName("Tests validation of a connector's name.")
    void testIsValid() {
        ConnectorRepository connectorRepositoryMock = mock(ConnectorRepository.class);
        Connector connectorMock = mock(Connector.class);
        Connector existingConnectorMock = mock(Connector.class);

        UniqueConnectorNameValidator validator = new UniqueConnectorNameValidator(connectorRepositoryMock);

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid(connectorMock, null));

        when(connectorMock.getName()).thenReturn("connector-mock");
        when(connectorRepositoryMock.findByName(eq("connector-mock"))).thenReturn(existingConnectorMock);

        when(connectorMock.getId()).thenReturn("id");
        when(existingConnectorMock.getId()).thenReturn("id");
        assertTrue(validator.isValid(connectorMock, null));

        when(existingConnectorMock.getId()).thenReturn("other-id");
        assertFalse(validator.isValid(connectorMock, null));
    }

}
