import {
    emailReceiverKebap,
    emailReceiverName,
    emailSenderKebap,
    emailSenderName,
    ftpServerKebap,
    ftpServerName,
    localFilesystemKebap,
    localFilesystemName,
    rabbitMqServerKebap,
    rabbitMqServerName,
    scpConnectorKebap,
    scpConnectorName,
    standardHttpKebap,
    standardHttpName
} from "../../support/before";

describe('Initializes the test environment\'s connectors', () => {

    it('Create local filesystem connector if required.', function () {
        cy.openConnectorOverview();
        cy.get('body').then((body) => {
            if (body.find(`[data-e2e=tile-${localFilesystemKebap}]`).length === 0) {
                cy.createConnector(localFilesystemName, 'File', 'Filesystem');
            }
        });
    });

    it('Create standard HTTP connector if required.', function () {
        cy.openConnectorOverview();
        cy.get('body').then((body) => {
            if (body.find(`[data-e2e=tile-${standardHttpKebap}]`).length === 0) {
                cy.createConnector(standardHttpName, 'Web', null, {
                    'testUrl': `http://${Cypress.env('nginx_host')}:${Cypress.env('nginx_port')}/igor-web.html`
                });
            }
        });
    });

    it('Create RabbitMQ connector if required.', function () {
        cy.openConnectorOverview();
        cy.get('body').then((body) => {
            if (body.find(`[data-e2e=tile-${rabbitMqServerKebap}]`).length === 0) {
                cy.createConnector(rabbitMqServerName, 'Message', 'RabbitMQ', {
                    'host': Cypress.env('rabbitmq_host'),
                    'port': Cypress.env('rabbitmq_port'),
                    'username': 'igor',
                    'password': 'igor',
                });
            }
        });
    });

    it('Create FTP server connector if required.', function () {
        cy.openConnectorOverview();
        cy.get('body').then((body) => {
            if (body.find(`[data-e2e=tile-${ftpServerKebap}]`).length === 0) {
                cy.createConnector(ftpServerName, 'File', 'FTP',
                    {
                        'host': Cypress.env('vsftpd_host'),
                        'port': Cypress.env('vsftpd_port')
                    }, {
                        'username': 'igor',
                        'password': 'igor'
                    });
            }
        });
    });

    it('Create SCP connector if required.', function () {
        cy.openConnectorOverview();
        cy.get('body').then((body) => {
            if (body.find(`[data-e2e=tile-${scpConnectorKebap}]`).length === 0) {
                cy.createConnector(scpConnectorName, 'File', 'SCP',
                    {
                        'host': Cypress.env('sshd_host'),
                        'port': Cypress.env('sshd_port'),
                        'username': 'igor',
                        'password': 'igor'
                    });
            }
        });
    });

    it('Create E-Mail Sender connector if required.', function () {
        cy.openConnectorOverview();
        cy.get('body').then((body) => {
            if (body.find(`[data-e2e=tile-${emailSenderKebap}]`).length === 0) {
                cy.createConnector(emailSenderName, 'Message', 'E-Mail Sender (SMTP)',
                    {
                        'host': Cypress.env('mail_host'),
                        'port': Cypress.env('mail_smtp_port'),
                        'username': 'igor',
                        'password': 'igor'
                    },
                    {
                        'alwaysTrustSsl': true
                    });
            }
        });
    });

    it('Create E-Mail Receiver connector if required.', function () {
        cy.openConnectorOverview();
        cy.get('body').then((body) => {
            if (body.find(`[data-e2e=tile-${emailReceiverKebap}]`).length === 0) {
                cy.createConnector(emailReceiverName, 'Message', 'E-Mail Receiver (IMAP)',
                    {
                        'host': Cypress.env('mail_host'),
                        'port': Cypress.env('mail_imap_port'),
                        'username': 'igor@igor-test.dev',
                        'password': 'igor'
                    },
                    {
                        'alwaysTrustSsl': true
                    });
            }
        });
    });

});
