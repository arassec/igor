export const localFilesystemName='Local Filesystem'
export const localFilesystemKebap='local-filesystem'
export const standardHttpName='Standard HTTP'
export const standardHttpKebap='standard-http'
export const rabbitMqServerName='RabbitMQ Server'
export const rabbitMqServerKebap='rabbit-mq-server'
export const ftpServerName='FTP Server'
export const ftpServerKebap='ftp-server'
export const scpConnectorName='SCP Connector'
export const scpConnectorKebap='scp-connector'

export const jobCopyNewFilesName='Copy New Files'
export const jobCopyNewFilesKebap='copy-new-files'

beforeEach(() => {
    cy.server();

    cy.route('GET', '/api/connector**').as('getConnectors')
    cy.route('GET', '/api/connector/**').as('getConnectorData')
    cy.route('GET', '/api/type/**').as('getTypes')
    cy.route('GET', '/api/parameters/**').as('getParameters')
    cy.route('POST', '/api/connector/test').as('testConnector')
    cy.route('POST', '/api/connector').as('saveConnector')
    cy.route('GET', '/api/job**').as('getJobs')
    cy.route('POST', '/api/job').as('saveJob')
    cy.route('GET', '/api/job/action/prototype').as('getActionPrototype')
    cy.route('POST', '/api/job/simulate').as('simulateJob')
    cy.route('POST', '/api/job/run').as('runJob')
    cy.route('GET', '/api/execution/job/**').as('getJobExecutions')
    cy.route('DELETE', '/api/job/**').as('deleteJob')
});
