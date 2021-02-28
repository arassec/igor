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

import 'cypress-file-upload';

Cypress.Commands.add('openConnectorOverview', () => {
    cy.visit('/');

    cy.get('[data-e2e=navigation-connector-overview]')
        .click();

    cy.wait(['@getConnectors'])

    cy.get('[data-e2e=navigation-heading]')
        .should('be.visible')
        .contains('Connector Overview');
});

Cypress.Commands.add('openConnectorEditor', (connector) => {
    cy.openConnectorOverview();

    if (connector) {
        cy.get(`[data-e2e=tile-${connector}`)
            .click();

        cy.wait(['@getConnectorData'])
    } else {
        cy.get('[data-e2e=connector-overview-add-connector]')
            .click();

        cy.get('[data-e2e=navigation-heading]')
            .should('be.visible')
            .contains('Connector-Editor');
    }
});

Cypress.Commands.add('createConnector', (name, category, type, parameters, advancedParameters) => {
    cy.log('Creating connector: ' + name);

    cy.openConnectorEditor();

    cy.get('[data-e2e=connector-name-input]')
        .type(name)

    cy.get('[data-e2e=connector-name-heading]')
        .should('be.visible')
        .contains(name)

    cy.get('[data-e2e=category-selector]')
        .select(category);

    cy.wait(['@getTypes'])

    if (type) {
        cy.get('[data-e2e=type-selector]')
            .select(type)
    }

    cy.wait(['@getParameters'])

    cy.setParameters(parameters, false);

    cy.setParameters(advancedParameters, true);

    cy.get('[data-e2e=connector-editor-test]')
        .click();

    cy.wait(['@testConnector'])

    cy.get('[data-e2e=snackbar]')
        .contains('Connection OK.');

    cy.get('[data-e2e=connector-editor-save]')
        .click();

    cy.wait(['@saveConnector'])

    cy.get('[data-e2e=snackbar]')
        .contains(`Connector '${name}' saved.`);
});

Cypress.Commands.add('deleteConnector', (kebabCaseName) => {
    cy.openConnectorOverview();

    cy.get(`[data-e2e=tile-${kebabCaseName}]`)
        .should('be.visible');

    cy.get(`[data-e2e=delete-${kebabCaseName}]`)
        .click();

    cy.get('[data-e2e=delete-connector-dialog]')
        .should('be.visible');

    cy.get('[data-e2e=delete-connector-confirm]')
        .should('be.visible')
        .click();

    cy.get(`[data-e2e=tile-${kebabCaseName}]`)
        .should('not.exist');
});

Cypress.Commands.add('setParameter', (name, value) => {
    cy.get('body').then((body) => {
        if (body.find(`[data-e2e=parameter-${name}]:visible button[data-e2e=picker]`).length > 0) {
            cy.get(`[data-e2e=parameter-${name}]:visible button[data-e2e=picker]`)
                .should('be.visible')
                .click();
            cy.get(`[data-e2e=picker-${value}]:visible`)
                .should('be.visible')
                .click();
            cy.get(`[data-e2e=parameter-${name}]:visible input`)
                .invoke('val')
                .should('not.be.empty');
        } else if (body.find(`[data-e2e=parameter-${name}]:visible input`).length > 0) {
            cy.get(`[data-e2e=parameter-${name}]:visible input`)
                .clear()
                .type(value)
            cy.get(`[data-e2e=parameter-${name}]:visible input`)
                .invoke('val')
                .should('not.be.empty');
        } else if (body.find(`[data-e2e=parameter-${name}]:visible textarea`).length > 0) {
            cy.get(`[data-e2e=parameter-${name}]:visible textarea`)
                .clear()
                .type(value)
            cy.get(`[data-e2e=parameter-${name}]:visible textarea`)
                .invoke('val')
                .should('not.be.empty')
        } else {
            cy.get(`[data-e2e=parameter-${name}]:visible`)
                .should('be.visible')
                .click();
        }
    });
})

Cypress.Commands.add('setParameters', (parameters, advanced) => {
    if (parameters) {
        if (advanced) {
            cy.get('[data-e2e=toggle-advanced-parameters]:visible')
                .click();
        }
        for (const [key, value] of Object.entries(parameters)) {
            cy.setParameter(key, value);
        }
    }
})

Cypress.Commands.add('openJobOverview', () => {
    cy.visit('/');

    cy.wait(['@getJobs'])

    cy.get('[data-e2e=navigation-heading]')
        .should('be.visible')
        .contains('Job Overview');
});

Cypress.Commands.add('openJobEditor', (job) => {
    cy.visit('/');

    cy.wait(['@getJobs'])

    cy.get(`[data-e2e=tile-${job}]`)
        .should('be.visible')
        .click();
});

Cypress.Commands.add('chooseCategory', (type, value) => {
    cy.get(`[data-e2e=${type}-category-selector]:visible`)
        .select(value);

    cy.wait(['@getTypes'])
    cy.wait(['@getParameters'])
});

Cypress.Commands.add('chooseType', (type, value) => {
    cy.get(`[data-e2e=${type}-type-selector]:visible`)
        .select(value);

    cy.wait(['@getParameters'])
});

Cypress.Commands.add('addAction', (name, category, type, parameters, advancedParameters) => {
    cy.get('[data-e2e=add-action-button]')
        .should('be.visible')
        .click();

    cy.wait(['@getTypes']);
    cy.wait(['@getActionPrototype']);

    cy.get('[data-e2e=name-input]:visible')
        .should('be.empty');

    if (name) {
        cy.get('[data-e2e=name-input]:visible')
            .type(name);

        cy.get('[data-e2e=title]:visible')
            .contains(name);
    }

    cy.chooseCategory('action', category);

    cy.chooseType('action', type);

    cy.setParameters(parameters, false);

    cy.setParameters(advancedParameters, true);
})

Cypress.Commands.add('chooseTrigger', (category, type, parameters, advancedParameters) => {
    cy.chooseCategory('trigger', category);

    cy.chooseType('trigger', type);

    cy.setParameters(parameters, false);

    cy.setParameters(advancedParameters, true);
})

Cypress.Commands.add('selectAction', (actionNameKebap) => {
    cy.get(`[data-e2e=action-${actionNameKebap}]`)
        .should('be.visible')
        .click();
})

Cypress.Commands.add('deleteJob', (jobName, jobKebap) => {
    cy.openJobOverview();

    cy.get(`[data-e2e=delete-${jobKebap}]`)
        .should('be.visible')
        .click();

    cy.get('[data-e2e=delete-job-confirm-button')
        .should('be.visible')
        .click();

    cy.wait('@deleteJob')

    cy.get('[data-e2e=snackbar]')
        .should('be.visible')
        .contains(`Job '${jobName}' has been deleted.`);
})

Cypress.Commands.add('simulateJob', () => {
    cy.get('[data-e2e=test-job-button]')
        .should('be.visible')
        .click()

    cy.wait('@simulateJob')

    cy.get('[data-e2e=snackbar]')
        .should('be.visible')
        .contains(`Simulation OK.`);

    cy.get('[data-e2e=dismiss-snackbar-button')
        .should('be.visible')
        .click();

    cy.get('[data-e2e=snackbar]')
        .should('not.exist')
})

Cypress.Commands.add('saveJob', () => {
    cy.get('[data-e2e=save-job-button').click();

    cy.wait(['@saveJob'])

    cy.get('[data-e2e=snackbar]')
        .contains(`saved.`);
})
