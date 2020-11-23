---
pageClass: docpage
---

# Job Editor

The job editor is used to configure a job. 
Additionally the execution history of the job is listed here.
The editor is split into three main sections.

![job editor](./images/job/job-editor.png)

## Job Configuration

The job configuration on the left side of the page is used to configure the job.

![job editor configuration](./images/job/job-editor-navigator.png)

The top element contains buttons for the following actions:

![switch navigator button](./images/job/switch-navigator-button.png) Toggles the view of the job configuration and the list of job executions.

![test button](./images/common/test-button.png) Runs the job in **simulation** mode. The results are displayed in the simulation results element.

![save button](./images/common/save-button.png) Saves the job.

![run job button](./images/job/run-job-button.png) Runs the job.

The 'run job' button might be disabled for the following reasons, indicated by the button's icon:

![run job disabled inactive](./images/job/run-job-button-inactive.png) The job is inactive.

![run job disabled waiting](./images/job/run-job-button-waiting.png) The job is waiting for a free execution slot.

![run job disabled running](./images/job/run-job-button-running.png) The job is already running.

![run job disabled active](./images/job/run-job-button-active.png) The job is triggered by events and already active.

![run job disabled failed](./images/job/run-job-button-failed.png) The job failed previously and is fault-intolerant.

Below the top element is the list of actions. 
The list can be ordered by drag-and-drop.
new Actions can be added to the job by pressing the ![add action](./images/job/add-action-button.png) button.

## Job Executions

The ![switch navigator button](./images/job/switch-navigator-button.png) opens or closes the job execution history.
It contains an entry for each past job execution.

![job editor executions](./images/job/job-editor-executions.png)

The number of saved execution entries can be configured for the job.
Details can be opened by clicking on an execution entry.

If an execution failed, its entry is red.
By clicking the ![mark resolved](./images/job/mark-resolved-button.png) button, the job execution can manually be marked as resolved.

::: tip
A job will not run if failed executions exist. 
You can either resolve the failed executions manually or configure the job to be fault tolerant (which is an advanced job configuration parameter).

Fault tolerant jobs are executed even if previous executions failed. 
A successful job execution will automatically mark all previously failed executions as resolved.
:::
 
## Parameters Editor

The parameters editor in the middle of the page contains all configuration parameters for the selected component.

![job editor configurator](./images/job/job-editor-configurator.png)

By selecting different categories and types for the components, the configuration parameters will change accordingly.

If an action is selected, the configuration parameters of the action are displayed.

The ![help button](./images/common/help-button.png) button can be used to display the online help for the selected component.

## Simulation Results

On the right of the page, the simulation results are displayed.

![job editor simulation results](./images/job/job-editor-simulation-results.png)

Each data item of the simulated job run is shown as it is supplied to the selected component.

By clicking on keys in the result JSON, a mustache template for the respective key is entered into the input above the simulation results.

A click on ![copy to clipboard button](./images/job/copy-to-clipboard-button.png) copies the mustache template to the clipboard.
