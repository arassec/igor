import {jobCopyNewFilesKebap} from "../../support/before";

const targetDir = '/examples/copynewfiles/';

describe('Creates examples doc images.', () => {
    it('Creates all images for the copy-new-files job example.', function () {
        cy.openJobEditor(jobCopyNewFilesKebap);

        cy.simulateJob();

        cy.get('[data-e2e=job-configurator]')
            .should('be.visible')
            .screenshot(targetDir + 'job-configuration');
        cy.get('[data-e2e=job-editor-simulation-results]')
            .should('be.visible')
            .screenshot(targetDir + 'job-configuration-sim-result');

        cy.selectAction('list-files')
        cy.get('[data-e2e=toggle-advanced-parameters]:visible')
            .should('be.visible')
            .click();
        cy.get('[data-e2e=action-configurator-list-files]')
            .should('be.visible')
            .screenshot(targetDir + 'list-files');
        cy.get('[data-e2e=job-editor-simulation-results]')
            .should('be.visible')
            .screenshot(targetDir + 'list-files-sim-result');

        cy.selectAction('filename-filter')
        cy.get('[data-e2e=toggle-advanced-parameters]:visible')
            .should('be.visible')
            .click();
        cy.get('[data-e2e=action-configurator-filename-filter]')
            .should('be.visible')
            .screenshot(targetDir + 'filename-filter');
        cy.get('[data-e2e=job-editor-simulation-results]')
            .should('be.visible')
            .screenshot(targetDir + 'filename-filter-sim-result');

        cy.selectAction('filter-already-copied-files')
        cy.get('[data-e2e=action-configurator-filter-already-copied-files]')
            .should('be.visible')
            .screenshot(targetDir + 'filter-already-copied-files');

        cy.selectAction('copy-file')
        cy.get('[data-e2e=action-configurator-copy-file]')
            .should('be.visible')
            .screenshot(targetDir + 'copy-file');
        cy.get('[data-e2e=job-editor-simulation-results]')
            .should('be.visible')
            .screenshot(targetDir + 'copy-file-sim-result');

        cy.selectAction('send-message')
        cy.get('[data-e2e=action-configurator-send-message]')
            .should('be.visible')
            .screenshot(targetDir + 'send-message');

        cy.selectAction('save-filename')
        cy.get('[data-e2e=action-configurator-save-filename]')
            .should('be.visible')
            .screenshot(targetDir + 'save-filename');

        cy.get('[data-e2e=job-configuration]')
            .should('be.visible')
            .click();

        cy.get('[data-e2e=side-menu]')
            .should('be.visible')
            .screenshot(targetDir + 'final-configuration')
    });
});
