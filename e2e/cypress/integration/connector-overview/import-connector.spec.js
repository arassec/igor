const connectorJson = 'connector-e-2-e-import-connector-test.igor.json'

it('Tests importing a connector.', function () {
    cy.openConnectorOverview();

    cy.get('[data-e2e=connector-overview-import-connector]')
        .should('be.visible')
        .click();

    cy.get('[data-e2e=import-connector-file-input]')
        .attachFile(connectorJson, { force: true });

    cy.get('[data-e2e=import-connector-chosen-file]')
        .should('be.visible')
        .contains(connectorJson);

    cy.get('[data-e2e=import-connector-confirm]')
        .should('be.visible')
        .click();

    cy.get('[data-e2e=tile-e-2-e-import-connector-test]')
        .should('be.visible')

    cy.get('[data-e2e=snackbar]')
        .should('be.visible')
        .contains('Import finished.');

    cy.deleteConnector('e-2-e-import-connector-test');
})