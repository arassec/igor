import {jobRabbitMqSenderKebap, jobRabbitMqSenderName, rabbitMqServerKebap} from "../../support/before";

describe('Initializes the test environment\'s \'RabbitMQ Sender\' job.', () => {

    it('Creates the job if required.', function () {
        cy.openJobOverview();

        cy.get('body').then((body) => {
            if (body.find(`[data-e2e=tile-${jobRabbitMqSenderKebap}]`).length === 0) {
                cy.get('[data-e2e=add-job-button]')
                    .should('be.visible')
                    .click();

                cy.wait(['@getTypes'])

                cy.get('[data-e2e=job-name-input] input')
                    .should('be.visible')
                    .clear()
                    .type(jobRabbitMqSenderName);

                cy.get('[data-e2e=job-title]')
                    .contains(jobRabbitMqSenderName);

                cy.addAction('Create Test Data', 'Test', 'Duplicate',
                    {
                        'amount': 1000
                    }, null);

                cy.addAction('Send to RabbitMQ', 'Message', 'Send RabbitMQ Message',
                    {
                        'messageConnector': rabbitMqServerKebap,
                        'exchange': 'igor-exchange',
                        'messageTemplate': '{{}{enter}  "test-message": "{{}{{}index{}}{}}"{enter}{}}'
                    }, null);

                cy.get('[data-e2e=save-job-button]')
                    .should('be.visible')
                    .click();

                cy.get('[data-e2e=snackbar]')
                    .should('be.visible')
                    .contains(`Job '${jobRabbitMqSenderName}' saved.`);

            }
        });
    });

});
