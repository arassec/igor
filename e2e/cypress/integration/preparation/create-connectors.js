import {ftpServerName, localFilesystemName, rabbitMqServerName, standardHttpName} from "../../support/before";

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
                    'testUrl': `http://igor-nginx:${Cypress.env('nginx_http_port')}/igor-web.html`
                });
            }
        });
    });

    it('Create RabbitMQ connector if required.', function () {
        cy.openConnectorOverview();
        cy.get('body').then((body) => {
            if (body.find('[data-e2e=tile-rabbit-mq-server]').length === 0) {
                cy.createConnector(rabbitMqServerName, 'Message', null, {
                    'host': 'igor-rabbitmq',
                    'port': Cypress.env('rabbitmq_port'),
                    'username': 'igor',
                    'password': 'igor',
                    'exchange': 'igor-test-exchange'
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
                        'host': 'igor-vsftpd',
                        'port': Cypress.env('vsftpd_port')
                    }, {
                        'username': 'igor',
                        'password': 'igor',
                        'passiveMode': false
                    });
            }
        });
    });

});