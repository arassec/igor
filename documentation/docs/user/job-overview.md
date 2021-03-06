---
pageClass: docpage
---

# Job Overview

The job overview is a dashboard displaying all configured jobs and the status of their last execution.
Jobs can be managed here, e.g. created, duplicated or imported from previous exports.

![job overview](./images/job/job-overview.png)

## Action Bar

On top of the page is the overview's action bar. 

![job overview action bar](./images/job/job-overview-action-bar.png)

On the left part are filter elements:

![job name filter](./images/job/job-name-filter.png) The name filter can be used to filter the displayed jobs by their name.

![job state filter](./images/job/job-state-filter.png) The buttons of the state filter can be used to filter jobs by their execution state.

On the right part of the action bar are buttons to perform the following actions:

![add job button](./images/job/add-job-button.png) Opens the job editor to create a new job.

![import job](./images/job/import-job-button.png) Opens the dialog for importing jobs.

![show schedule](./images/job/show-schedule-button.png) Opens the schedule that displays the next execution of jobs, that are triggered by CRON expressions.

## Job Tiles

The main element of the job overview are the job tiles.
Each job is displayed in a separate tile.
Clicking on the tile opens the job editor for the corresponding job.

![job-tile](./images/job/job-overview-tile.png)

The tile contains the following buttons to modify the job:

![delete job button](./images/common/delete-button.png) Deletes the job.

![export job](./images/common/export-button.png) Exports the job as JSON text file.

![duplicate job](./images/common/duplicate-button.png) Opens the job editor with a duplicated job configuration.

![run job](./images/job/run-job-button.png) Runs the job.

The 'run job' button might be disabled for the following reasons, indicated by the button's icon:

![run job disabled inactive](./images/job/run-job-button-inactive.png) The job is inactive.

![run job disabled waiting](./images/job/run-job-button-waiting.png) The job is waiting for a free execution slot.

![run job disabled running](./images/job/run-job-button-running.png) The job is already running.

![run job disabled active](./images/job/run-job-button-active.png) The job is triggered by events and already active.

![run job disabled failed](./images/job/run-job-button-failed.png) The job failed previously and is fault-intolerant.

Information about the last job execution is displayed at the bottom of the job tile.
You can open detailed information by clicking on it. 
