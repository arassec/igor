// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

Cypress.Commands.add('openConnectorOverview', () => {
    cy.visit('/');

    cy.get('[data-e2e=navigation-connector-overview]')
        .click();

    cy.get('[data-e2e=navigation-heading]')
        .should('be.visible')
        .contains('Connector Overview');
});

Cypress.Commands.add('openConnectorEditor', () => {
    cy.openConnectorOverview();

    cy.get('[data-e2e=connector-overview-add-connector]')
        .click();

    cy.get('[data-e2e=navigation-heading]')
        .should('be.visible')
        .contains('Connector-Editor');
});

Cypress.Commands.add('createConnector', (name, category, type) => {
    cy.openConnectorEditor();

    cy.get('[data-e2e=connector-name-input]')
        .type(name)

    cy.get('[data-e2e=connector-name-heading]')
        .should('be.visible')
        .contains(name)

    cy.get('[data-e2e=category-selector]')
        .select(category);

    cy.get('[data-e2e=type-selector]')
        .select(type)

    cy.get('[data-e2e=connector-editor-save]')
        .click();
});

Cypress.Commands.add('deleteConnector', (kebabCaseName) => {
    cy.openConnectorOverview();

    cy.get(`[data-e2e=connector-tile-${kebabCaseName}]`)
        .should('be.visible');

    cy.get(`[data-e2e=delete-${kebabCaseName}]`)
        .click();

    cy.get('[data-e2e=delete-connector-dialog]')
        .should('be.visible');

    cy.get('[data-e2e=delete-connector-confirm]')
        .should('be.visible')
        .click();

    cy.get(`[data-e2e=connector-tile-${kebabCaseName}]`)
        .should('not.exist');
});
