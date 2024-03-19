import {
    ftpServerKebap,
    jobCopyNewFilesKebap,
    jobCopyNewFilesName,
    rabbitMqServerKebap,
    scpConnectorKebap,
} from "../../support/before";

describe("Initializes the test environment's 'Copy New Files' job.", () => {
    it("Creates the job if required.", function () {
        cy.openJobOverview();

        cy.get("body").then((body) => {
            if (
                body.find(`[data-e2e=tile-${jobCopyNewFilesKebap}]`).length ===
                0
            ) {
                cy.get("[data-e2e=add-job-button]")
                    .should("be.visible")
                    .click();

                cy.wait(["@getTypes"]);

                cy.get("[data-e2e=job-name-input] input")
                    .should("be.visible")
                    .clear()
                    .type(jobCopyNewFilesName);

                cy.get("[data-e2e=job-title]").contains(jobCopyNewFilesName);

                cy.chooseCategory("trigger", "Util");

                cy.chooseType("trigger", "CRON");

                cy.get("[data-e2e=parameter-cronExpression] button")
                    .should("be.visible")
                    .click();

                cy.get("[data-e2e=every-fiveteen-minutes-cron]")
                    .should("be.visible")
                    .click();

                cy.get("[data-e2e=parameter-cronExpression] input")
                    .should("be.visible")
                    .invoke("val")
                    .should("eq", "0 */15 * * * *");

                cy.addAction(
                    "List Files",
                    "File",
                    "List Files",
                    {
                        source: ftpServerKebap,
                        directory: ".",
                    },
                    {
                        fileEnding: "txt",
                    }
                );

                cy.addAction(
                    "Filename Filter",
                    "Util",
                    "Filter by Regular Expression",
                    {
                        input: "{{}{{}data.filename{}}{}}",
                        expression: ".*backup.*",
                    },
                    {
                        dropMatching: true,
                    }
                );

                cy.addAction(
                    "Filter Already Copied Files",
                    "Persistence",
                    "Filter Persisted Value",
                    {
                        input: "{{}{{}data.filename{}}{}}",
                    }
                );

                cy.addAction("Copy File", "File", "Copy File", {
                    source: ftpServerKebap,
                    sourceDirectory: "{{}{{}data.directory{}}{}}",
                    sourceFilename: "{{}{{}data.filename{}}{}}",
                    target: scpConnectorKebap,
                    targetDirectory: "/tmp/",
                    targetFilename: "{{}{{}data.filename{}}{}}",
                });

                cy.addAction(
                    "Send Message",
                    "Message",
                    "Send RabbitMQ Message",
                    {
                        messageConnector: rabbitMqServerKebap,
                        exchange: "igor-exchange",
                        messageTemplate:
                            '{{}{enter}  "file": "{{}{{}data.filename{}}{}}"{enter}{}}',
                    }
                );

                cy.addAction("Save Filename", "Persistence", "Persist Value", {
                    input: "{{}{{}data.filename{}}{}}",
                    numValuesToKeep: "1",
                });

                cy.get("[data-e2e=save-job-button]")
                    .should("be.visible")
                    .click();

                cy.get("[data-e2e=snackbar]")
                    .should("be.visible")
                    .contains(`Job '${jobCopyNewFilesName}' saved.`);

                cy.get("[data-e2e=run-job-button]")
                    .should("be.visible")
                    .click();

                cy.get("[data-e2e=run-job-confirm-button]")
                    .should("be.visible")
                    .click();

                cy.get("[data-e2e=snackbar]")
                    .should("be.visible")
                    .contains(`Job '${jobCopyNewFilesName}' started manually.`);
            }
        });
    });
});
