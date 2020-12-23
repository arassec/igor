import {localFilesystemKebap} from "../../support/before";

it('Tests duplicating a connector.', function () {
    cy.openConnectorOverview();

    cy.get(`[data-e2e=duplicate-${localFilesystemKebap}]`)
        .should('be.visible')
        .click();

    cy.get('[data-e2e=connector-name-input] input')
        .should('be.visible')
        .clear()
        .type('E2E Duplicate Connector Test');

    cy.get('[data-e2e=connector-name-heading]')
        .contains('E2E Duplicate Connector Test');

    cy.get('[data-e2e=connector-editor-save]')
        .should('be.visible')
        .click();

    cy.get('[data-e2e=snackbar]')
        .should('be.visible')
        .contains('Connector \'E2E Duplicate Connector Test\' saved.');

    cy.deleteConnector('e-2-e-duplicate-connector-test')
})
