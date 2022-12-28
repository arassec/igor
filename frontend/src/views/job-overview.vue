<template>
    <core-container class="wrap">
        <action-bar data-e2e="job-overview-action-bar">
            <template v-slot:left>
                <div class="action-bar-container">
                    <input-filter
                        :filter-key="'job-name-filter'"
                        :filter="filterJobName"
                        :label="'Name filter:'"
                        data-e2e="job-name-filter"
                    />
                    <div data-e2e="job-state-filter">
                        <label id="state-filter-label">State filter:</label>
                        <toggle-button
                            icon="circle-notch"
                            :label="
                                'Running/Active (' +
                                executionsOverview.numRunning +
                                '/' +
                                executionsOverview.numSlots +
                                ')'
                            "
                            bgcolor="var(--color-foreground)"
                            class="margin-right"
                            fontcolor="var(--color-font)"
                            v-on:selected="filterJobStateRunning"
                            :selected="stateFilter.running"
                        />
                        <toggle-button
                            icon="hourglass"
                            :label="'Waiting (' + executionsOverview.numWaiting + ')'"
                            bgcolor="var(--color-foreground)"
                            class="margin-right"
                            fontcolor="var(--color-font)"
                            v-on:selected="filterJobStateWaiting"
                            :selected="stateFilter.waiting"
                        />
                        <toggle-button
                            icon="bolt"
                            :label="'Failed (' + executionsOverview.numFailed + ')'"
                            bgcolor="var(--color-alert)"
                            class="margin-right"
                            fontcolor="var(--color-font)"
                            v-on:selected="filterJobStateFailed"
                            :selected="stateFilter.failed"
                        />
                    </div>
                </div>
            </template>
            <template v-slot:right>
                <div>
                    <router-link :to="'job-editor'">
                        <input-button icon="plus" label="Add job" class="margin-right" data-e2e="add-job-button" />
                    </router-link>
                    <input-button
                        icon="file-upload"
                        label="Import job"
                        class="margin-right"
                        v-on:clicked="openShowImportDialog"
                        data-e2e="import-job-button"
                    />
                    <input-button
                        icon="clipboard"
                        label="Show schedule"
                        v-on:clicked="openScheduleDialog"
                        data-e2e="show-schedule-button"
                    />
                </div>
            </template>
        </action-bar>

        <div class="tiles-container">
            <div class="tiles">
                <div v-for="job of jobsPage.items" :key="job.id">
                    <overview-tile
                        v-on:clicked="editJob(job.id)"
                        v-on:action-clicked="openExecutionDetailsDialog(job)"
                        :active="job.active"
                        :title-content="job.name"
                    >
                        <template v-slot:title>
                            {{ job.name }}
                        </template>
                        <template v-slot:menu>
                            <layout-row>
                                <template v-slot:left>
                                    <input-button
                                        icon="trash"
                                        v-on:clicked="openDeleteJobDialog(job.id, job.name)"
                                        class="margin-right"
                                        :data-e2e="dataE2EName('delete-', job.name)"
                                    />
                                    <input-button
                                        icon="file-download"
                                        v-on:clicked="openExportDialog(job.id, job.name)"
                                        class="margin-right"
                                        :data-e2e="dataE2EName('export-', job.name)"
                                    />
                                    <input-button
                                        icon="clone"
                                        v-on:clicked="duplicateJob(job.id)"
                                        class="margin-right"
                                        :data-e2e="dataE2EName('duplicate-', job.name)"
                                    />
                                </template>
                                <template v-slot:right>
                                    <input-button
                                        :icon="getRunJobIcon(job)"
                                        v-on:clicked="openRunJobDialog(job.id, job.name)"
                                        :disabled="isRunJobDisabled(job)"
                                        :data-e2e="dataE2EName('run-', job.name)"
                                    />
                                </template>
                            </layout-row>
                        </template>
                        <template v-slot:action>
                            <div
                                :class="isJobInState(job, ['FAILED']) ? 'alert' : 'info'"
                                :data-e2e="dataE2EName('state-', job.name)"
                            >
                                <icon-button
                                    icon="times"
                                    v-on:clicked="openCancelJobDialog(job.execution.id, job.name)"
                                    v-if="isJobInState(job, ['WAITING', 'RUNNING', 'ACTIVE'])"
                                    class="right"
                                />
                                <icon-button
                                    icon="check"
                                    v-on:clicked="
                                        openMarkJobExecutionResolvedDialog(job.execution.id, job.id, job.name)
                                    "
                                    v-if="isJobInState(job, ['FAILED'])"
                                    class="right"
                                />
                                <font-awesome-icon
                                    icon="circle-notch"
                                    class="fa-fw fa-spin"
                                    v-if="isJobInState(job, ['RUNNING'])"
                                />
                                <font-awesome-icon
                                    icon="sign-in-alt"
                                    class="fa-fw"
                                    v-if="isJobInState(job, ['ACTIVE'])"
                                />
                                <font-awesome-icon
                                    icon="hourglass"
                                    class="fa-fw"
                                    v-if="isJobInState(job, ['WAITING'])"
                                />
                                <font-awesome-icon icon="bolt" class="fa-fw" v-if="isJobInState(job, ['FAILED'])" />
                                <font-awesome-icon
                                    icon="bolt"
                                    class="fa-fw has-failed-executions"
                                    v-if="job.hasFailedExecutions && !isJobInState(job, ['FAILED'])"
                                />
                                {{ formatJobState(job) }}
                            </div>
                        </template>
                    </overview-tile>
                </div>
            </div>

            <list-pager
                :page="jobsPage"
                v-if="jobsPage && jobsPage.totalPages > 1"
                :dark="true"
                v-on:first="loadJobs(0)"
                v-on:previous="loadJobs(jobsPage.number - 1)"
                v-on:next="loadJobs(jobsPage.number + 1)"
                v-on:last="loadJobs(jobsPage.totalPages - 1)"
            />
        </div>

        <modal-dialog v-show="showRunDialog" @close="showRunDialog = false" v-on:cancel="showRunDialog = false">
            <template v-slot:header>
                <h1>Start job?</h1>
            </template>
            <template v-slot:body>
                Manually start job
                <div class="truncate highlight">{{ selectedJobName }}</div>
                now?
            </template>
            <template v-slot:footer>
                <layout-row>
                    <template v-slot:left>
                        <input-button v-on:clicked="showRunDialog = false" icon="times" />
                    </template>
                    <template v-slot:right>
                        <input-button v-on:clicked="runJob()" icon="check" data-e2e="confirm-run-job-manually-button" />
                    </template>
                </layout-row>
            </template>
        </modal-dialog>

        <modal-dialog
            v-show="showCancelJobDialog"
            @close="showCancelJobDialog = false"
            v-on:cancel="showCancelJobDialog = false"
        >
            <template v-slot:header>
                <h1>Cancel job execution?</h1>
            </template>
            <template v-slot:body>
                Are you sure you want to cancel this execution of job:
                <div class="truncate highlight">{{ selectedJobName }}</div>
            </template>
            <template v-slot:footer>
                <layout-row>
                    <template v-slot:left>
                        <input-button v-on:clicked="showCancelJobDialog = false" icon="times" />
                    </template>
                    <template v-slot:right>
                        <input-button v-on:clicked="cancelJobExecution()" icon="check" />
                    </template>
                </layout-row>
            </template>
        </modal-dialog>

        <modal-dialog
            v-show="showMarkJobExecutionResolvedDialog"
            @close="showMarkJobExecutionResolvedDialog = false"
            v-on:cancel="showMarkJobExecutionResolvedDialog = false"
        >
            <template v-slot:header>
                <h1>Mark job execution as resolved</h1>
            </template>
            <template v-slot:body>
                <div class="paragraph">Mark this failed execution as resolved?</div>
                <div class="paragraph" v-if="numFailedExecutionsForSelectedJob > 1">
                    Mark
                    <strong>all {{ numFailedExecutionsForSelectedJob }}</strong>
                    executions of job
                    <div class="truncate highlight">{{ selectedJobName }}</div>
                    as resolved:
                    <font-awesome-icon
                        :icon="resolveAllFailedExecutionsOfJob ? 'check-square' : 'square'"
                        v-on:click="resolveAllFailedExecutionsOfJob = !resolveAllFailedExecutionsOfJob"
                    />
                </div>
            </template>
            <template v-slot:footer>
                <layout-row>
                    <template v-slot:left>
                        <input-button v-on:clicked="showMarkJobExecutionResolvedDialog = false" icon="times" />
                    </template>
                    <template v-slot:right>
                        <input-button v-on:clicked="markJobExecutionResolved()" icon="check" />
                    </template>
                </layout-row>
            </template>
        </modal-dialog>

        <modal-dialog
            v-show="showExportDialog"
            @close="showExportDialog = false"
            v-on:cancel="showExportDialog = false"
        >
            <template v-slot:header>
                <h1>Export job?</h1>
            </template>
            <template v-slot:body>
                <div class="paragraph">
                    Export job
                    <div class="truncate highlight">{{ selectedJobName }}</div>
                    to file?
                </div>
                <div class="paragraph alert">
                    WARNING: the created file will contain passwords and other sensitive information in cleartext!
                </div>
            </template>
            <template v-slot:footer>
                <layout-row>
                    <template v-slot:left>
                        <input-button v-on:clicked="showExportDialog = false" icon="times" />
                    </template>
                    <template v-slot:right>
                        <input-button v-on:clicked="exportJob()" icon="check" />
                    </template>
                </layout-row>
            </template>
        </modal-dialog>

        <modal-dialog
            v-show="showScheduleDialog"
            @close="showScheduleDialog = false"
            v-on:cancel="showScheduleDialog = false"
        >
            <template v-slot:header>
                <layout-row>
                    <template v-slot:left>
                        <h1>Scheduled jobs</h1>
                    </template>
                    <template v-slot:right>
                        <input-button icon="times" v-on:clicked="showScheduleDialog = false" />
                    </template>
                </layout-row>
            </template>
            <template v-slot:body>
                <label v-if="!schedulePage || !schedulePage.items || schedulePage.items.length === 0">
                    There are currently no scheduled jobs.
                </label>
                <div class="schedule-box">
                    <feedback-box
                        v-for="scheduledJob in schedulePage.items"
                        :key="scheduledJob.jobId"
                        :clickable="true"
                        v-on:feedback-clicked="editJob(scheduledJob.jobId)"
                    >
                        <template v-slot:left>
                            <div class="margin-right truncate">
                                {{ scheduledJob.jobName }}
                            </div>
                        </template>
                        <template v-slot:right>
                            <div>
                                {{ formatTimestamp(scheduledJob.nextRun) }}
                            </div>
                        </template>
                    </feedback-box>
                </div>
            </template>
            <template v-slot:footer>
                <list-pager
                    :page="schedulePage"
                    v-on:first="loadSchedule(0)"
                    v-on:previous="loadSchedule(schedulePage.number - 1)"
                    v-on:next="loadSchedule(schedulePage.number + 1)"
                    v-on:last="loadSchedule(schedulePage.totalPages - 1)"
                />
            </template>
        </modal-dialog>

        <modal-dialog
            v-show="showImportDialog"
            @close="showImportDialog = false"
            v-on:cancel="showImportDialog = false"
        >
            <template v-slot:header>
                <h1>Import data?</h1>
            </template>
            <template v-slot:body>
                <div class="paragraph">Select a previously exported JSON file to import.</div>
                <div class="paragraph alert">
                    WARNING: existing connectors and jobs will be overwritten by the import!
                </div>
                <div class="paragraph margin-top">
                    <label for="import-file-selector" id="import-file-select">
                        <font-awesome-icon icon="folder-open" />
                        Select file
                        <input id="import-file-selector" type="file" @change="importFileChanged" />
                    </label>
                    <label v-if="importFile != null">{{ importFile.name }}</label>
                </div>
            </template>
            <template v-slot:footer>
                <layout-row>
                    <template v-slot:left>
                        <input-button v-on:clicked="showImportDialog = false" icon="times" />
                    </template>
                    <template v-slot:right>
                        <input-button v-on:clicked="executeImport" icon="check" />
                    </template>
                </layout-row>
            </template>
        </modal-dialog>

        <job-execution-details
            v-if="selectedJobExecution"
            v-show="showExecutionDetailsDialog"
            v-bind:job-execution="selectedJobExecution"
            v-on:close="closeExecutionDetailsDialog()"
        />

        <delete-job-dialog
            v-if="showDeleteDialog"
            v-bind:job-id="selectedJobId"
            v-bind:job-name="selectedJobName"
            v-on:cancel="showDeleteDialog = false"
            v-on:delete-plus="deleteJob(true)"
            v-on:delete="deleteJob(false)"
        />

        <background-icon icon="toolbox" />
    </core-container>
</template>

<script>
import OverviewTile from "@/components/common/overview-tile.vue";
import IgorBackend from "@/utils/igor-backend.js";
import Utils from "@/utils/utils.js";
import DeleteJobDialog from "@/components/jobs/delete-job-dialog.vue";
import LayoutRow from "@/components/common/layout-row.vue";
import ModalDialog from "@/components/common/modal-dialog.vue";
import InputButton from "@/components/common/input-button.vue";
import IconButton from "@/components/common/icon-button.vue";
import ListPager from "@/components/common/list-pager.vue";
import ActionBar from "@/components/common/action-bar.vue";
import BackgroundIcon from "@/components/common/background-icon.vue";
import InputFilter from "@/components/common/input-filter.vue";
import FeedbackBox from "@/components/common/feedback-box.vue";
import JobExecutionDetails from "@/components/jobs/job-execution-details.vue";
import ToggleButton from "@/components/common/toggle-button.vue";
import CoreContainer from "@/components/common/core-container.vue";
import { useWipStore } from "@/stores/wip";
import { useTupleStore } from "@/stores/tuple";
import { useJobDataStore } from "@/stores/jobdata";
import { useConnectorDataStore } from "@/stores/connectordata";

export default {
    name: "job-overview",
    components: {
        CoreContainer,
        ToggleButton,
        JobExecutionDetails,
        FeedbackBox,
        InputFilter,
        BackgroundIcon,
        ActionBar,
        ListPager,
        IconButton,
        InputButton,
        ModalDialog,
        LayoutRow,
        DeleteJobDialog,
        OverviewTile,
    },
    data: function () {
        return {
            jobsPage: {
                number: 0,
                size: 15,
                totalPages: 0,
                items: [],
            },
            schedulePage: {
                number: 0,
                size: 10,
                totalPages: 0,
                items: [],
            },
            executionsOverview: {
                numSlots: 0,
                numRunning: 0,
                numWaiting: 0,
                numFailed: 0,
            },
            nameFilter: "",
            stateFilter: {
                running: false,
                waiting: false,
                failed: false,
            },
            showDeleteDialog: false,
            showRunDialog: false,
            showExportDialog: false,
            showCancelJobDialog: false,
            showScheduleDialog: false,
            showImportDialog: false,
            selectedJobId: null,
            selectedJobName: null,
            selectedJobExecutionId: null,
            selectedJobExecution: null,
            numFailedExecutionsForSelectedJob: 0,
            showMarkJobExecutionResolvedDialog: false,
            resolveAllFailedExecutionsOfJob: false,
            importFile: null,
            showExecutionDetailsDialog: false,
            jobListEventSource: null,
            jobExecutionEventSource: null,
        };
    },
    methods: {
        formatJobState: function (job) {
            if (job.execution == null) {
                return "Idle";
            }
            let formattedState = Utils.capitalize(job.execution.state.toLowerCase()) + ": ";
            if (this.isJobInState(job, ["RUNNING", "WAITING"])) {
                formattedState += job.execution.duration;
            } else {
                formattedState += Utils.formatInstant(job.execution.created);
            }
            return formattedState;
        },
        isJobInState: function (job, states) {
            if (job.execution == null) {
                return false;
            }
            return states.includes(job.execution.state);
        },
        getRunJobIcon: function (job) {
            if (this.isJobInState(job, "ACTIVE")) {
                return "sign-in-alt";
            } else if (this.isJobInState(job, "WAITING")) {
                return "hourglass";
            } else if (this.isJobInState(job, "RUNNING")) {
                return "circle-notch";
            } else if (job.hasFailedExecutions && !job.faultTolerant) {
                return "bolt";
            }
            return "play";
        },
        isRunJobDisabled: function (job) {
            return (
                this.isJobInState(job, ["WAITING", "RUNNING", "ACTIVE"]) ||
                (job.hasFailedExecutions && !job.faultTolerant) ||
                !job.active
            );
        },
        loadJobs: async function (page) {
            if (page === undefined) {
                page = this.jobsPage.number;
            }
            if (this.jobsPage) {
                let stateFilter = "";
                if (this.stateFilter.running) {
                    stateFilter += "RUNNING,ACTIVE,";
                }
                if (this.stateFilter.waiting) {
                    stateFilter += "WAITING,";
                }
                if (this.stateFilter.failed) {
                    stateFilter += "FAILED,";
                }
                if (stateFilter.length > 0) {
                    stateFilter = stateFilter.substring(0, stateFilter.length - 1);
                }
                IgorBackend.getData(
                    "/api/job?pageNumber=" +
                        page +
                        "&pageSize=" +
                        this.jobsPage.size +
                        "&nameFilter=" +
                        this.nameFilter +
                        "&stateFilter=" +
                        stateFilter
                )
                    .then((data) => {
                        this.jobsPage = data;
                    })
                    .catch((error) => {
                        console.error("Error during backend request: " + error);
                    });
            }
        },
        editJob: function (jobId) {
            this.$router.push({ name: "job-editor", params: { jobId: jobId } });
        },
        openDeleteJobDialog: function (jobId, jobName) {
            this.selectedJobId = jobId;
            this.selectedJobName = jobName;
            this.showDeleteDialog = true;
        },
        deleteJob: function (deleteExclusiveConnectors) {
            this.showDeleteDialog = false;
            IgorBackend.deleteData(
                "/api/job/" + this.selectedJobId + "?deleteExclusiveConnectors=" + deleteExclusiveConnectors,
                "Deleting job",
                "Job '" + Utils.formatNameForSnackbar(this.selectedJobName) + "' has been deleted.",
                "Job '" + Utils.formatNameForSnackbar(this.selectedJobName) + "' could not be deleted!"
            )
                .then(() => {
                    this.loadJobs();
                })
                .catch((error) => {
                    console.error("Error during backend request: " + error);
                });
        },
        openRunJobDialog: function (jobId, jobName) {
            this.selectedJobId = jobId;
            this.selectedJobName = jobName;
            this.showRunDialog = true;
        },
        runJob: function () {
            IgorBackend.postData(
                "/api/job/run/" + this.selectedJobId,
                null,
                "Starting job",
                "Job '" + Utils.formatNameForSnackbar(this.selectedJobName) + "' started manually.",
                "Job '" + Utils.formatNameForSnackbar(this.selectedJobName) + "' startup failed"
            )
                .then(() => {
                    this.showRunDialog = false;
                })
                .catch((error) => {
                    console.error("Error during backend request: " + error);
                });
        },
        openExportDialog: function (jobId, jobName) {
            this.selectedJobId = jobId;
            this.selectedJobName = jobName;
            this.showExportDialog = true;
        },
        exportJob: function () {
            this.showExportDialog = false;
            IgorBackend.getResponse("/api/transfer/job/" + this.selectedJobId)
                .then((response) => {
                    const url = window.URL.createObjectURL(new Blob([JSON.stringify(response.data)]));
                    const link = document.createElement("a");
                    link.href = url;
                    let fileName = "job-" + Utils.toKebabCase(this.selectedJobName) + ".igor.json";
                    link.setAttribute("download", fileName);
                    document.body.appendChild(link);
                    link.click();
                })
                .catch((error) => {
                    console.error("Error during backend request: " + error);
                });
        },
        openCancelJobDialog: function (jobExecutionId, jobName) {
            this.selectedJobExecutionId = jobExecutionId;
            this.selectedJobName = jobName;
            this.showCancelJobDialog = true;
        },
        cancelJobExecution: function () {
            this.showCancelJobDialog = false;
            IgorBackend.postData(
                "/api/execution/" + this.selectedJobExecutionId + "/cancel",
                null,
                "Cancelling job",
                "Job cancelled.",
                "Job could not be cancelled!"
            ).catch((error) => {
                console.error("Error during backend request: " + error);
            });
        },
        openMarkJobExecutionResolvedDialog: async function (jobExecutionId, jobId, jobName) {
            this.selectedJobExecutionId = jobExecutionId;
            this.selectedJobId = jobId;
            this.selectedJobName = jobName;
            useWipStore().setWip("Loading job execution details...", undefined);
            this.numFailedExecutionsForSelectedJob = await IgorBackend.getData(
                "/api/execution/job/" + this.selectedJobId + "/FAILED/count"
            ).catch((error) => {
                console.error("Error during backend request: " + error);
            });
            useWipStore().clearWip();
            this.showMarkJobExecutionResolvedDialog = true;
        },
        markJobExecutionResolved: async function () {
            await IgorBackend.putData(
                "/api/execution/" +
                    this.selectedJobExecutionId +
                    "/" +
                    this.selectedJobId +
                    "/FAILED/RESOLVED?updateAllOfJob=" +
                    this.resolveAllFailedExecutionsOfJob,
                null,
                "Updating executions",
                "Executions updated",
                "Executions could not be updated!"
            ).catch((error) => {
                console.error("Error during backend request: " + error);
            });
            this.resolveAllFailedExecutionsOfJob = false;
            this.showMarkJobExecutionResolvedDialog = false;
        },
        duplicateJob: async function (id) {
            let jobConfiguration = await IgorBackend.getData("/api/job/" + id).catch((error) => {
                console.error("Error during backend request: " + error);
            });
            jobConfiguration.name = "Copy of " + jobConfiguration.name;
            jobConfiguration.id = Utils.uuidv4();
            jobConfiguration.trigger.id = Utils.uuidv4();
            useJobDataStore().setJobData(jobConfiguration, "-1_-1", -1, "");
            this.$router.push({ name: "job-editor" });
        },
        formatTimestamp: function (timestamp) {
            return Utils.formatInstant(timestamp);
        },
        filterJobName: function (filterSelectionFromSelector) {
            this.nameFilter = filterSelectionFromSelector;
            this.loadJobs(0);
        },
        filterJobStateRunning: function () {
            this.stateFilter.running = !this.stateFilter.running;
            useTupleStore().setValue("job-state-filter", this.stateFilter);
            this.loadJobs(0);
        },
        filterJobStateWaiting: function () {
            this.stateFilter.waiting = !this.stateFilter.waiting;
            useTupleStore().setValue("job-state-filter", this.stateFilter);
            this.loadJobs(0);
        },
        filterJobStateFailed: function () {
            this.stateFilter.failed = !this.stateFilter.failed;
            useTupleStore().setValue("job-state-filter", this.stateFilter);
            this.loadJobs(0);
        },
        openShowImportDialog: function () {
            this.importFile = null;
            this.showImportDialog = true;
        },
        importFileChanged: function (event) {
            this.importFile = event.target.files[0];
        },
        executeImport: function () {
            if (!this.importFile) {
                this.showImportDialog = false;
            } else {
                let reader = new FileReader();
                reader.onload = (e) => {
                    this.showImportDialog = false;
                    IgorBackend.postData(
                        "/api/transfer",
                        JSON.parse(e.target.result),
                        "Importing job",
                        "Import finished",
                        "Import failed"
                    )
                        .then(() => {
                            this.importFile = null;
                        })
                        .catch((error) => {
                            console.error("Error during backend request: " + error);
                        });
                };
                reader.readAsText(this.importFile);
            }
        },
        openScheduleDialog: async function () {
            await this.loadSchedule(0);
            this.showScheduleDialog = true;
        },
        loadSchedule: async function (page) {
            this.schedulePage = await IgorBackend.getData(
                "/api/job/schedule?pageNumber=" + page + "&pageSize=" + this.schedulePage.size
            ).catch((error) => {
                console.error("Error during backend request: " + error);
            });
        },
        openExecutionDetailsDialog: async function (job) {
            if (job.execution == null) {
                return;
            }
            this.selectedJobExecutionId = job.execution.id;
            this.selectedJobExecution = await IgorBackend.getData(
                "/api/execution/details/" + this.selectedJobExecutionId
            ).catch((error) => {
                console.error("Error during backend request: " + error);
            });
            this.showExecutionDetailsDialog = true;
            if (
                this.selectedJobExecution.executionState === "WAITING" ||
                this.selectedJobExecution.executionState === "RUNNING"
            ) {
                this.initJobExecutionEventSource();
            }
        },
        closeExecutionDetailsDialog: function () {
            if (this.jobExecutionEventSource) {
                this.jobExecutionEventSource.close();
            }
            this.showExecutionDetailsDialog = false;
        },
        dataE2EName: function (prefix, suffix) {
            return prefix + Utils.toKebabCase(suffix);
        },
        initJobListEventSource: function () {
            if (this.jobListEventSource) {
                this.jobListEventSource.close();
            }
            this.jobListEventSource = new EventSource("api/job/stream");
            let component = this;
            this.jobListEventSource.addEventListener(
                "state-update",
                function (event) {
                    let jobListEntry = JSON.parse(event.data);
                    let elementIndex = -1;
                    component.jobsPage.items.forEach(function (item, index) {
                        if (item.id === jobListEntry.id) {
                            elementIndex = index;
                        }
                    });
                    if (elementIndex !== -1) {
                        component.jobsPage.items[elementIndex] = jobListEntry;
                    }
                },
                false
            );
            this.jobListEventSource.addEventListener(
                "execution-overview",
                function (event) {
                    component.executionsOverview = JSON.parse(event.data);
                },
                false
            );
            this.jobListEventSource.addEventListener(
                "crud",
                function () {
                    component.loadJobs();
                },
                false
            );
            this.jobListEventSource.onerror = () => {
                setTimeout(this.initJobListEventSource, 5000);
            };
        },
        initJobExecutionEventSource: function () {
            this.jobExecutionEventSource = new EventSource("api/execution/stream");
            let component = this;
            this.jobExecutionEventSource.onmessage = (event) => {
                let jobExecutionDetails = JSON.parse(event.data);
                if (jobExecutionDetails.id === component.selectedJobExecutionId) {
                    component.selectedJobExecution = jobExecutionDetails;
                }
            };
            this.jobExecutionEventSource.onerror = () => {
                setTimeout(this.initJobListEventSource, 5000);
            };
        },
    },
    mounted() {
        useConnectorDataStore().clearConnectorData();
        useJobDataStore().clearJobData();
        if (useTupleStore().getValue("job-name-filter")) {
            this.nameFilter = useTupleStore().getValue("job-name-filter");
        }
        if (useTupleStore().getValue("job-state-filter")) {
            this.stateFilter = useTupleStore().getValue("job-state-filter");
        }
        this.loadJobs().then(() => {
            this.initJobListEventSource();
        });
    },
    unmounted() {
        if (this.jobListEventSource) {
            this.jobListEventSource.close();
        }
        if (this.jobExecutionEventSource) {
            this.jobExecutionEventSource.close();
        }
    },
};
</script>

<style scoped>
.wrap {
    flex-wrap: wrap;
}

.tiles-container {
    display: flex;
    flex-direction: column;
    width: 100%;
}

.tiles {
    display: flex;
    flex-wrap: wrap;
    width: 100%;
}

.action-bar-container {
    display: flex;
    flex-direction: row;
}

.right {
    float: right;
}

.has-failed-executions {
    color: var(--color-alert);
}

#state-filter-label {
    margin: 0.15em 0.25em 0 0;
    color: var(--color-font);
}

.margin-top {
    margin: 1.25em 0 1em 0;
}

input[type="file"] {
    display: none;
}

#import-file-select {
    border: 1px solid var(--color-font);
    padding: 0.25em;
    background-color: var(--color-background);
    color: var(--color-font);
    margin: 0 1em 0 0;
}

#import-file-select:hover {
    cursor: pointer;
    background-color: var(--color-font);
    color: var(--color-background);
}
</style>
