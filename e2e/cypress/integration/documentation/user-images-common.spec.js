const targetDir = '/user/common/';

describe('Creates user doc common images.', () => {
    it('Create delete-button.png, export-button.png and duplicate-button.png', function () {
        cy.openConnectorOverview();

        cy.get('[data-e2e=delete-local-filesystem]')
            .should('be.visible')
            .screenshot(targetDir + 'delete-button')

        cy.get('[data-e2e=export-local-filesystem]')
            .should('be.visible')
            .screenshot(targetDir + 'export-button')

        cy.get('[data-e2e=duplicate-local-filesystem]')
            .should('be.visible')
            .screenshot(targetDir + 'duplicate-button')
    });

    it('Create help-button.png, test-button.png and save-button.png', function () {
        cy.openConnectorEditor();

        cy.get('[data-e2e=main-help-button]')
            .should('be.visible')
            .screenshot(targetDir + 'help-button');

        cy.get('[data-e2e=connector-editor-test]')
            .should('be.visible')
            .screenshot(targetDir + 'test-button');

        cy.get('[data-e2e=connector-editor-save]')
            .should('be.visible')
            .screenshot(targetDir + 'save-button');
    });
});