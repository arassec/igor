import {jobCopyNewFilesKebap, standardHttpKebap} from "../../support/before";

const targetDir = '/user/job/';

export const activeJobName = "E2E Active Job"
export const activeJobKebap = "e-2-e-active-job"

const createActiveJob = function (nameSuffix) {
    let jobName = activeJobName + nameSuffix;

    cy.openJobOverview();

    cy.get('[data-e2e=add-job-button]')
        .should('be.visible')
        .click();

    cy.wait(['@getTypes'])

    cy.get('[data-e2e=job-name-input] input')
        .should('be.visible')
        .clear()
        .type(jobName);

    cy.get('[data-e2e=job-title]')
        .contains(jobName);

    cy.chooseCategory('trigger', 'Web');

    cy.chooseType('trigger', 'Web Hook');

    cy.get('[data-e2e=save-job-button]')
        .should('be.visible')
        .click();

    cy.get('[data-e2e=snackbar]')
        .should('be.visible')
        .contains(`Job '${jobName}' saved.`);

}

const deleteActiveJob = function (nameSuffix) {
    let jobName = activeJobName + nameSuffix;
    let jobKebap = activeJobKebap + nameSuffix;

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
}

describe('Creates user doc job images.', () => {

    describe('Creates Job-Overview images.', () => {
        it('Create job-overview.png and job-overview-tile.png', function () {
            cy.openJobOverview();

            cy.get('[data-e2e=app-content]')
                .should('be.visible')
                .screenshot(targetDir + 'job-overview');

            cy.get('[data-e2e=tile-copy-new-files]')
                .should('be.visible')
                .screenshot(targetDir + 'job-overview-tile')
        });

        it('Create job-overview-action-bar.png', function () {
            cy.openJobOverview();

            cy.get('[data-e2e=job-overview-action-bar]')
                .should('be.visible')
                .screenshot(targetDir + 'job-overview-action-bar');
        });

        it('Create job-name-filter.png and job-state-filter.png', function () {
            cy.openJobOverview();

            cy.get('[data-e2e=job-name-filter]')
                .should('be.visible')
                .screenshot(targetDir + 'job-name-filter');

            cy.get('[data-e2e=job-state-filter]')
                .should('be.visible')
                .screenshot(targetDir + 'job-state-filter');
        });

        it('Create add-job-button.png, import-job-button.png and show-schedule-button.png', function () {
            cy.openJobOverview();

            cy.get('[data-e2e=add-job-button]')
                .should('be.visible')
                .screenshot(targetDir + 'add-job-button');

            cy.get('[data-e2e=import-job-button]')
                .should('be.visible')
                .screenshot(targetDir + 'import-job-button');

            cy.get('[data-e2e=show-schedule-button]')
                .should('be.visible')
                .screenshot(targetDir + 'show-schedule-button');
        });
    });

    describe('Creates Job-Editor images.', () => {
        it('Create job-editor.png, job-editor-configurator.png, job-editor-navigator.png, job-editor-executions.png and job-editor-simulation-results.png', function () {
            cy.openJobEditor(jobCopyNewFilesKebap);

            cy.simulateJob();

            cy.get('[data-e2e=app-content]')
                .should('be.visible')
                .screenshot(targetDir + 'job-editor');

            cy.get('[data-e2e=job-configurator]')
                .should('be.visible')
                .screenshot(targetDir + 'job-editor-configurator')

            cy.get('[data-e2e=side-menu]')
                .should('be.visible')
                .screenshot(targetDir + 'job-editor-navigator')

            cy.get('[data-e2e=job-editor-simulation-results]')
                .should('be.visible')
                .screenshot(targetDir + 'job-editor-simulation-results')

            cy.get('[data-e2e=show-job-executions-button]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=job-editor-executions]')
                .should('be.visible')
                .screenshot(targetDir + 'job-editor-executions')
        });

        it('Create add-new-action-button.png, switch-navigator-button.png and run-button.png', function () {
            cy.openJobEditor(jobCopyNewFilesKebap);

            cy.selectAction('list-files');

            cy.get('[data-e2e=add-action-button]')
                .should('be.visible')
                .screenshot(targetDir + 'add-action-button')

            cy.get('[data-e2e=show-job-executions-button]')
                .should('be.visible')
                .screenshot(targetDir + 'switch-navigator-button')

            cy.get('[data-e2e=run-job-button]')
                .should('be.visible')
                .screenshot(targetDir + 'run-job-button')
        });

        it('Create mark-resolved-button.png and run-job-button-failed.png', function () {
            let jobName = 'E2E Failing Job'
            let jobKebap = 'e-2-e-failing-job'

            cy.openJobOverview();

            cy.get('[data-e2e=add-job-button]')
                .should('be.visible')
                .click();

            cy.wait(['@getTypes'])

            cy.get('[data-e2e=job-name-input] input')
                .should('be.visible')
                .clear()
                .type(jobName);

            cy.get('[data-e2e=job-title]')
                .contains(jobName);

            cy.get('[data-e2e=toggle-advanced-job-parameters]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=job-faulttolerant-button]')
                .should('be.visible')
                .click();

            cy.chooseCategory('trigger', 'Util');

            cy.chooseType('trigger', 'Manual');

            cy.addAction('Failing Web Request', 'Web', 'HTTP Request',
                {
                    'httpConnector': standardHttpKebap,
                    'url': 'http://invalid.url'
                });

            cy.get('[data-e2e=run-job-button]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=run-job-confirm-button]')
                .should('be.visible')
                .click();

            cy.wait('@runJob')

            cy.wait('@getJobExecutions')

            cy.get('[data-e2e=job-execution-0-mark-resolved-button]')
                .should('be.visible')
                .screenshot(targetDir + 'mark-resolved-button')

            cy.get('[data-e2e=show-job-configuration-button')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=run-job-button]')
                .should('be.visible')
                .screenshot(targetDir + 'run-job-button-failed');

            cy.deleteJob(jobName, jobKebap);
        });

        it('Create run-job-button-inactive.png, run-job-button-active.png, run-job-button-running.png, run-job-button-waiting.png', function () {
            let jobName = 'E2E Waiting Job'
            let jobKebap = 'e-2-e-waiting-job'

            createActiveJob('1');

            cy.openJobOverview();

            cy.get(`[data-e2e=run-${activeJobKebap}1] svg`)
                .should('have.class', 'fa-sign-in-alt');

            cy.get(`[data-e2e=run-${activeJobKebap}1]`)
                .should('be.visible')
                .screenshot(targetDir + 'run-job-button-active')

            // Default number of job execution slots: 5
            createActiveJob('2');
            createActiveJob('3');
            createActiveJob('4');
            createActiveJob('5');

            cy.openJobOverview();

            cy.get('[data-e2e=add-job-button]')
                .should('be.visible')
                .click();

            cy.wait(['@getTypes'])

            cy.get('[data-e2e=job-name-input] input')
                .should('be.visible')
                .clear()
                .type(jobName);

            cy.get('[data-e2e=job-title]')
                .contains(jobName);

            cy.chooseCategory('trigger', 'Util');

            cy.chooseType('trigger', 'Manual');

            cy.addAction('Pause', 'Util', 'Pause');

            cy.get('[data-e2e=job-configuration]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=job-active-button]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=save-job-button]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=snackbar]')
                .should('be.visible')
                .contains(`Job '${jobName}' saved.`);

            cy.openJobOverview();

            cy.get(`[data-e2e=run-${jobKebap}]`)
                .should('be.visible')
                .screenshot(targetDir + 'run-job-button-inactive');

            cy.openJobEditor(jobKebap);

            cy.get('[data-e2e=job-active-button]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=run-job-button]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=run-job-confirm-button]')
                .should('be.visible')
                .click();

            cy.wait('@runJob')

            cy.get('[data-e2e=show-job-configuration-button]')
                .should('be.visible')
                .click();

            cy.selectAction('pause')

            cy.get('[data-e2e=run-job-button]')
                .should('be.visible')
                .screenshot(targetDir + 'run-job-button-waiting');

            cy.deleteJob(jobName, jobKebap);

            deleteActiveJob('1');
            deleteActiveJob('2');
            deleteActiveJob('3');
            deleteActiveJob('4');
            deleteActiveJob('5');
        });

        it('Create run-job-button-running.png', function () {
            let jobName = 'E2E Running Job'
            let jobKebap = 'e-2-e-running-job'

            cy.openJobOverview();

            cy.get('[data-e2e=add-job-button]')
                .should('be.visible')
                .click();

            cy.wait(['@getTypes'])

            cy.get('[data-e2e=job-name-input] input')
                .should('be.visible')
                .clear()
                .type(jobName);

            cy.get('[data-e2e=job-title]')
                .contains(jobName);

            cy.chooseCategory('trigger', 'Util');

            cy.chooseType('trigger', 'Manual');

            cy.addAction('Pause', 'Util', 'Pause',
                {
                    'milliseconds': 60000
                });

            cy.get('[data-e2e=run-job-button]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=run-job-confirm-button]')
                .should('be.visible')
                .click();

            cy.wait('@runJob')

            cy.get('[data-e2e=show-job-configuration-button]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=run-job-button]')
                .should('be.visible')
                .screenshot(targetDir + 'run-job-button-running');

            cy.deleteJob(jobName, jobKebap)
        });

        it.only('Create copy-to-clipboard.png', function () {
            cy.openJobEditor(jobCopyNewFilesKebap);

            cy.simulateJob();

            cy.get('[data-e2e=copy-to-clipboard-button]')
                .should('be.visible')
                .screenshot(targetDir + 'copy-to-clipboard-button')
        });
    });
});