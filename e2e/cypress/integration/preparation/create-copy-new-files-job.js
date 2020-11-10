import {ftpServerKebap, localFilesystemKebap, rabbitMqServerKebap} from "../../support/before";

describe('Initializes the test environment\'s \'Copy New Files\' job.', () => {

    it('Creates the job if required.', function () {
        cy.openJobOverview();
        cy.get('body').then((body) => {
            if (body.find('[data-e2e=tile-copy-new-files]').length === 0) {
                cy.route('GET', '/api/type/**').as('getTypes')

                cy.get('[data-e2e=add-job-button]')
                    .should('be.visible')
                    .click();

                cy.wait(['@getTypes'])

                cy.get('[data-e2e=job-name-input] input')
                    .should('be.visible')
                    .clear()
                    .type('Copy New Files');

                cy.get('[data-e2e=job-title]')
                    .contains("Copy New Files");

                cy.chooseCategory('trigger', 'Util');

                cy.chooseType('trigger', 'CRON');

                cy.get('[data-e2e=parameter-cronExpression] button')
                    .should('be.visible')
                    .click();

                cy.get('[data-e2e=every-fiveteen-minutes-cron]')
                    .should('be.visible')
                    .click();

                cy.get('[data-e2e=parameter-cronExpression] input')
                    .should('be.visible')
                    .invoke('val')
                    .should('eq', '0 */15 * * * *');

                cy.addAction('List Files', 'File', 'List Files',
                    {
                        'source': ftpServerKebap,
                        'directory': '.'
                    },
                    {
                        'fileEnding': 'txt'
                    });

                cy.addAction('Filename Filter', 'Util', 'Filter by Regular Expression',
                    {
                        'input': '{{}{{}data.filename{}}{}}',
                        'expression': '.*backup.*'
                    },
                    {
                        'dropMatching': true
                    });

                cy.addAction('Filter Already Copied Files', 'Persistence', 'Filter Persisted Value',
                    {
                        'input': '{{}{{}data.filename{}}{}}'
                    });

                cy.addAction('Copy File', 'File', 'Copy File',
                    {
                        'source': ftpServerKebap,
                        'sourceDirectory': '{{}{{}data.directory{}}{}}',
                        'sourceFilename': '{{}{{}data.filename{}}{}}',
                        'target': localFilesystemKebap,
                        'targetDirectory': '/volume1/data/test',
                        'targetFilename': '{{}{{}data.filename{}}{}}'
                    });

                cy.addAction('Send Message', 'Message', 'Send Message',
                    {
                        'messageConnector': rabbitMqServerKebap,
                        'messageTemplate': '{{}{enter}  "file": "{{}{{}data.filename{}}{}}"{enter}{}}'
                    });

                cy.addAction('Save Filename', 'Persistence', 'Persist Value',
                    {
                        'input': '{{}{{}data.filename{}}{}}',
                        'numValuesToKeep': '5000'
                    });

                cy.get('[data-e2e=save-job-button]')
                    .should('be.visible')
                    .click();

                cy.get('[data-e2e=snackbar]')
                    .contains(`Job 'Copy New Files' saved.`);
            }
        });
    });

});
