import {
    jobEmailReceiverKebap,
    jobEmailSenderKebap,
    jobRabbitMqReceiverKebap,
    jobRabbitMqSenderKebap
} from "../../support/before";

describe('Tests message sending and receiving.', () => {
    it('Tests sending and receiving messages with RabbitMQ.', function () {
            cy.openJobEditor(jobRabbitMqReceiverKebap);

            let checked;

            // Deactivate the listener:
            cy.get('[data-e2e=job-active-button]').then(elem => {
                checked = elem.hasClass('fa-check-square')
                if (checked) {
                    cy.get('[data-e2e=job-active-button]')
                        .click();
                }
            })

            cy.get('[data-e2e=job-active-button]')
                .should('have.class', 'fa-square');

            cy.saveJob();

            // Send messages with the sender:
            cy.openJobOverview();

            cy.get(`[data-e2e=run-${jobRabbitMqSenderKebap}]`)
                .should('be.visible')
                .click();

            cy.get('[data-e2e=confirm-run-job-manually-button]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=confirm-run-job-manually-button]')
                .should('not.be.visible');

            cy.get(`[data-e2e=state-${jobRabbitMqSenderKebap}]`)
                .contains('Running');

            cy.get(`[data-e2e=state-${jobRabbitMqSenderKebap}]`)
                .contains('Finished');

            // Receive messages with the listener:
            cy.openJobEditor(jobRabbitMqReceiverKebap);

            cy.get('[data-e2e=job-active-button]')
                .click();

            cy.saveJob();

            // Verify at least some messages have been received:
            cy.openJobOverview();

            cy.get(`[data-e2e=state-${jobRabbitMqReceiverKebap}]`)
                .contains('Active')
                .click();

            cy.get('[data-e2e=job-execution-details-events]')
                .should('be.visible'); // If it is shown, at least one event has been processed!

            cy.get('[data-e2e=close-execution-details-button]')
                .should('be.visible')
                .click();

            // Restore received 'active' state:
            if (checked) {
                cy.get('[data-e2e=job-active-button]')
                    .click();

                cy.saveJob();
            }
        }
    );

    it('Tests sending and receiving E-Mails.', function () {
            // Consume any previous mails:
            cy.openJobOverview();

            cy.get(`[data-e2e=run-${jobEmailReceiverKebap}]`)
                .should('be.visible')
                .click();

            cy.get('[data-e2e=confirm-run-job-manually-button]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=confirm-run-job-manually-button]')
                .should('not.be.visible');

            cy.get(`[data-e2e=state-${jobEmailReceiverKebap}]`)
                .contains('Running');

            cy.get(`[data-e2e=state-${jobEmailReceiverKebap}]`)
                .contains('Finished');

            // Send a single E-Mail with the sender:
            cy.openJobOverview();

            cy.get(`[data-e2e=run-${jobEmailSenderKebap}]`)
                .should('be.visible')
                .click();

            cy.get('[data-e2e=confirm-run-job-manually-button]')
                .should('be.visible')
                .click();

            cy.get('[data-e2e=confirm-run-job-manually-button]')
                .should('not.be.visible');

            cy.get(`[data-e2e=state-${jobEmailSenderKebap}]`)
                .contains('Running');

            cy.get(`[data-e2e=state-${jobEmailSenderKebap}]`)
                .contains('Finished');

            // Receive messages with the listener:
            cy.openJobEditor(jobEmailReceiverKebap);

            cy.get('[data-e2e=test-job-button]')
                .should('be.visible')
                .click();

            cy.selectAction('receive-mails');

            cy.get('[data-e2e=show-sim-results-json-0]')
                .should('be.visible')
                .click();

            cy.get(':nth-child(9) > .vjs-key')
                .should('be.visible')
                .contains('"message":')
        }
    );
});
