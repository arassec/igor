import {
    ftpServerName,
    localFilesystemName,
    rabbitMqServerName,
    scpConnectorName,
    standardHttpName
} from "../../support/before";

describe('Initializes the test environment\'s connectors', () => {

    it('Create local filesystem connector if required.', function () {
        cy.openConnectorOverview();
        cy.get('body').then((body) => {
            if (body.find('[data-e2e=tile-local-filesystem]').length === 0) {
                cy.createConnector(localFilesystemName, 'File', 'Filesystem');
            }
        });
    });

    it('Create standard HTTP connector if required.', function () {
        cy.openConnectorOverview();
        cy.get('body').then((body) => {
            if (body.find('[data-e2e=tile-standard-http]').length === 0) {
                cy.createConnector(standardHttpName, 'Web', null, {
                    'testUrl': `http://${Cypress.env('nginx_host')}:${Cypress.env('nginx_port')}/igor-web.html`
                });
            }
        });
    });

    it('Create RabbitMQ connector if required.', function () {
        cy.openConnectorOverview();
        cy.get('body').then((body) => {
            if (body.find('[data-e2e=tile-rabbit-mq-server]').length === 0) {
                cy.createConnector(rabbitMqServerName, 'Message', null, {
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
            if (body.find('[data-e2e=tile-ftp-server]').length === 0) {
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
            if (body.find('[data-e2e=tile-scp-connector]').length === 0) {
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

});
