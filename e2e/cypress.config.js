const {defineConfig} = require('cypress')

module.exports = defineConfig({
    viewportWidth: 1920,
    viewportHeight: 1080,
    trashAssetsBeforeRuns: true,
    defaultCommandTimeout: 60000,
    requestTimeout: 60000,
    env: {
        nginx_host: 'igor-nginx',
        nginx_port: 80,
        rabbitmq_host: 'igor-rabbitmq',
        rabbitmq_port: 5672,
        vsftpd_host: 'igor-vsftpd',
        vsftpd_port: 21,
        sshd_host: 'igor-sshd',
        sshd_port: 22,
        mail_host: 'igor-mail',
        mail_smtp_port: 25,
        mail_imap_port: 143,
    },
    watchForFileChanges: false,
    e2e: {
        // We've imported your old cypress plugins here.
        // You may want to clean this up later by importing these.
        setupNodeEvents(on, config) {
            return require('./cypress/plugins/index.js')(on, config)
        },
        baseUrl: 'http://localhost:9090',
        specPattern: [
            'cypress/e2e/preparation/create-connectors.js',
            'cypress/e2e/preparation/create-copy-new-files-job.js',
            'cypress/e2e/preparation/create-rabbitmq-sender-job.js',
            'cypress/e2e/preparation/create-rabbitmq-receiver-job.js',
            'cypress/e2e/preparation/create-email-sender-job.js',
            'cypress/e2e/preparation/create-email-receiver-job.js',
            'cypress/e2e/documentation/*.spec.js',
            'cypress/e2e/connector-overview/*.spec.js',
            'cypress/e2e/jobs/*.spec.js',
        ],
        experimentalRunAllSpecs: true
    },
})
