describe('Creates all images used in the user documentation.', () => {

    before(() => {
        cy.createConnector('Local Filesystem', 'File', 'Filesystem');
    });

    after(() => {
        cy.deleteConnector('local-filesystem');
    })

    describe('Creates Connctor-Overview images.', () => {
        it('Create connector-overview.png', function () {

            cy.openConnectorOverview();

            cy.get('[data-e2e=app-content]')
                .should('be.visible')
                .screenshot('connector-overview');
        });

        it('Create connector-overview-action-bar.png', function () {

            cy.openConnectorOverview();

            cy.get('[data-e2e=connector-overview-action-bar]')
                .should('be.visible')
                .screenshot('connector-overview-action-bar');
        });

        it('Create connector-name-filter.png, add-connector-button.png and import-connector-button.png', function () {

            cy.openConnectorOverview();

            cy.get('[data-e2e=connector-overview-name-filter]')
                .should('be.visible')
                .screenshot('connector-name-filter');

            cy.get('[data-e2e=connector-overview-add-connector]')
                .should('be.visible')
                .screenshot('add-connector-button');

            cy.get('[data-e2e=connector-overview-import-connector]')
                .should('be.visible')
                .screenshot('import-connector-button');
        });
    });

    describe('Creates Connctor-Editor images.', () => {
        it('Create test-button.png and save-button.png', function () {

            cy.openConnectorEditor();

            cy.get('[data-e2e=connector-editor-test]')
                .should('be.visible')
                .screenshot('test-button');

            cy.get('[data-e2e=connector-editor-save]')
                .should('be.visible')
                .screenshot('save-button');
        });
    });

});