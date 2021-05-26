import {emailSenderKebap, jobEmailSenderKebap, jobEmailSenderName} from "../../support/before";

describe('Initializes the test environment\'s \'E-Mail Sender\' job.', () => {

    it('Creates the job if required.', function () {
        cy.openJobOverview();

        cy.get('body').then((body) => {
            if (body.find(`[data-e2e=tile-${jobEmailSenderKebap}]`).length === 0) {
                cy.get('[data-e2e=add-job-button]')
                    .should('be.visible')
                    .click();

                cy.wait(['@getTypes'])

                cy.get('[data-e2e=job-name-input] input')
                    .should('be.visible')
                    .clear()
                    .type(jobEmailSenderName);

                cy.get('[data-e2e=job-title]')
                    .contains(jobEmailSenderName);

                cy.addAction('Send E-Mail', 'Message', 'Send E-Mail',
                    {
                        'emailConnector': emailSenderKebap,
                        'from': 'job@igor-test.dev',
                        'to': 'igor@igor-test.dev',
                        'subject': 'Test Mail',
                        'body': 'Hallo from job: {{}{{}meta.jobId{}}{}}.'
                    }, null);

                cy.get('[data-e2e=save-job-button]')
                    .should('be.visible')
                    .click();

                cy.get('[data-e2e=snackbar]')
                    .should('be.visible')
                    .contains(`Job '${jobEmailSenderName}' saved.`);

            }
        });
    });

});
