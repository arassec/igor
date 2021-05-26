import {emailReceiverKebap, jobEmailReceiverKebap, jobEmailReceiverName} from "../../support/before";

describe('Initializes the test environment\'s \'E-Mail Receiver\' job.', () => {

    it('Creates the job if required.', function () {
        cy.openJobOverview();

        cy.get('body').then((body) => {
            if (body.find(`[data-e2e=tile-${jobEmailReceiverKebap}]`).length === 0) {
                cy.get('[data-e2e=add-job-button]')
                    .should('be.visible')
                    .click();

                cy.wait(['@getTypes'])

                cy.get('[data-e2e=job-name-input] input')
                    .should('be.visible')
                    .clear()
                    .type(jobEmailReceiverName);

                cy.get('[data-e2e=job-title]')
                    .contains(jobEmailReceiverName);

                cy.addAction('Receive Mails', 'Message', 'Receive E-Mail',
                    {
                        'emailConnector': emailReceiverKebap
                    });

                cy.addAction('Log Messages', 'Test', 'Log', null, null);

                cy.get('[data-e2e=save-job-button]')
                    .should('be.visible')
                    .click();

                cy.get('[data-e2e=snackbar]')
                    .should('be.visible')
                    .contains(`Job '${jobEmailReceiverName}' saved.`);
            }
        });
    });

});
