package com.arassec.igor.core.model.annotation.validation;

import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.repository.ConnectorRepository;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates a connector's name and checks, that it is unique.
 * <p>
 * This could not be implemented as field validator because the ID of the connector is required, additional to the name. The bean
 * validation API does not support access to other fields within a field validator.
 */
public class UniqueConnectorNameValidator implements ConstraintValidator<UniqueConnectorName, Connector> {

    /**
     * Repository for connectors.
     */
    private final ConnectorRepository connectorRepository;

    /**
     * Creates a new validator instance.
     *
     * @param connectorRepository The repository for connectors.
     */
    public UniqueConnectorNameValidator(ConnectorRepository connectorRepository) {
        this.connectorRepository = connectorRepository;
    }

    /**
     * Takes the supplied connector's name and loads an existing connector with the same name (if any). The existing connector
     * must have the same ID as the new connector or else it will be rejected.
     *
     * @param connector                  The connector to check.
     * @param constraintValidatorContext The validator context.
     *
     * @return {@code true} if the supplied connector is valid, {@code false} if another connector with the same name already
     * exists.
     */
    @Override
    public boolean isValid(Connector connector, ConstraintValidatorContext constraintValidatorContext) {
        if (connector == null) {
            return true;
        }

        var existingConnectorWithSameName = connectorRepository.findByName(connector.getName());

        if (existingConnectorWithSameName != null) {
            return existingConnectorWithSameName.getId().equals(connector.getId());
        }

        return true;
    }

}
