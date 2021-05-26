import {jobRabbitMqReceiverKebap, jobRabbitMqReceiverName, rabbitMqServerKebap} from "../../support/before";

describe('Initializes the test environment\'s \'RabbitMQ Receiver\' job.', () => {

    it('Creates the job if required.', function () {
        cy.openJobOverview();

        cy.get('body').then((body) => {
            if (body.find(`[data-e2e=tile-${jobRabbitMqReceiverKebap}]`).length === 0) {
                cy.get('[data-e2e=add-job-button]')
                    .should('be.visible')
                    .click();

                cy.wait(['@getTypes'])

                cy.get('[data-e2e=job-name-input] input')
                    .should('be.visible')
                    .clear()
                    .type(jobRabbitMqReceiverName);

                cy.get('[data-e2e=job-title]')
                    .contains(jobRabbitMqReceiverName);

                cy.chooseTrigger('Message', 'RabbitMQ Message',
                    {
                        'rabbitMqConnector': rabbitMqServerKebap,
                        'queueName': 'igor-queue'
                    }, null);

                cy.addAction('Pause', 'Test', 'Pause',
                    {
                        'milliseconds': 1000
                    },
                    {
                        'variance': 500
                    });

                cy.addAction('Log Messages', 'Test', 'Log', null, null);

                cy.get('[data-e2e=save-job-button]')
                    .should('be.visible')
                    .click();

                cy.get('[data-e2e=snackbar]')
                    .should('be.visible')
                    .contains(`Job '${jobRabbitMqReceiverName}' saved.`);
            }
        });
    });

});
