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
export const emailSenderName='E-Mail Sender'
export const emailSenderKebap='e-mail-sender'
export const emailReceiverName='E-Mail Receiver'
export const emailReceiverKebap='e-mail-receiver'

export const jobCopyNewFilesName='Copy New Files'
export const jobCopyNewFilesKebap='copy-new-files'
export const jobRabbitMqSenderName='RabbitMQ Sender'
export const jobRabbitMqSenderKebap="rabbit-mq-sender"
export const jobRabbitMqReceiverName='RabbitMQ Receiver'
export const jobRabbitMqReceiverKebap="rabbit-mq-receiver"
export const jobEmailSenderName='E-Mail Sender'
export const jobEmailSenderKebap='e-mail-sender'
export const jobEmailReceiverName='E-Mail Receiver'
export const jobEmailReceiverKebap='e-mail-receiver'

beforeEach(() => {
    cy.intercept('GET', '/api/connector**').as('getConnectors')
    cy.intercept('GET', '/api/connector/**').as('getConnectorData')
    cy.intercept('GET', '/api/type/**').as('getTypes')
    cy.intercept('GET', '/api/parameters/**').as('getParameters')
    cy.intercept('POST', '/api/connector/test').as('testConnector')
    cy.intercept('POST', '/api/connector').as('saveConnector')
    cy.intercept('GET', '/api/job**').as('getJobs')
    cy.intercept('POST', '/api/job').as('saveJob')
    cy.intercept('GET', '/api/job/action/prototype').as('getActionPrototype')
    cy.intercept('POST', '/api/job/simulate').as('simulateJob')
    cy.intercept('POST', '/api/job/run').as('runJob')
    cy.intercept('GET', '/api/execution/job/**').as('getJobExecutions')
    cy.intercept('DELETE', '/api/job/**').as('deleteJob')
});
