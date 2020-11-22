const targetDir = '/user/connector/';

describe('Creates user doc connector images.', () => {

    describe('Creates Connector-Overview images.', () => {
        it('Create connector-overview.png and connector-overview-tile.png', function () {
            cy.openConnectorOverview();

            cy.get('[data-e2e=app-content]')
                .should('be.visible')
                .screenshot(targetDir + 'connector-overview');

            cy.get('[data-e2e=tile-rabbit-mq-server]')
                .should('be.visible')
                .screenshot(targetDir + 'connector-overview-tile')
        });

        it('Create connector-overview-action-bar.png', function () {
            cy.openConnectorOverview();

            cy.get('[data-e2e=connector-overview-action-bar]')
                .should('be.visible')
                .screenshot(targetDir + 'connector-overview-action-bar');
        });

        it('Create connector-name-filter.png, add-connector-button.png and import-connector-button.png', function () {
            cy.openConnectorOverview();

            cy.get('[data-e2e=connector-overview-name-filter]')
                .should('be.visible')
                .screenshot(targetDir + 'connector-name-filter');

            cy.get('[data-e2e=connector-overview-add-connector]')
                .should('be.visible')
                .screenshot(targetDir + 'add-connector-button');

            cy.get('[data-e2e=connector-overview-import-connector]')
                .should('be.visible')
                .screenshot(targetDir + 'import-connector-button');
        });
    });

    describe('Creates Connector-Editor images.', () => {
        it('Create connector-editor.png, connector-editor-configurator.png and connector-editor-navigator.png', function () {
            cy.openConnectorEditor('rabbit-mq-server');

            cy.get('[data-e2e=app-content]')
                .should('be.visible')
                .screenshot(targetDir + 'connector-editor');

            cy.get('[data-e2e=connector-configurator]')
                .should('be.visible')
                .screenshot(targetDir + 'connector-editor-configurator')

            cy.get('[data-e2e=side-menu]')
                .should('be.visible')
                .screenshot(targetDir + 'connector-editor-navigator')
        });

        it('Create return-to-job.png', function () {
            cy.openConnectorEditor('rabbit-mq-server');

            cy.get('[data-e2e=connector-editor-return-to-job]')
                .should('be.visible')
                .screenshot(targetDir + 'return-to-job-button');
        });
    });

});