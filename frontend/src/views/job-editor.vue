<template>
    <core-container>

        <side-menu v-if="jobConfiguration">
            <job-navigation slot="content"
                            :job-configuration="jobConfiguration"
                            :selected-task-id="selectedTaskId"
                            :selected-action-id="selectedActionId"
                            :job-running-or-waiting="jobRunningOrWaiting"
                            :validation-errors="validationErrors"
                            :job-executions-page="jobExecutionsPage"
                            v-on:cancel-configuration="cancelConfiguration"
                            v-on:test-configuration="testConfiguration"
                            v-on:save-configuration="saveConfiguration"
                            v-on:run-job="showRunDialog = true"
                            v-on:job-is-selected="selectJob"
                            v-on:task-is-selected="selectTask"
                            v-on:action-is-selected="selectAction"
                            v-on:add-task="addTask"
                            v-on:add-action="addAction"
                            v-on:delete-task="showDeleteTask"
                            v-on:delete-action="showDeleteAction"
                            v-on:duplicate-task="duplicateTask"/>
            <core-panel slot="footer" v-if="jobExecutionsPage.items.length > 0">
                <h1>Job Executions</h1>
                <feedback-box
                        v-for="(jobExecution, index) in jobExecutionsPage.items"
                        :key="index" :alert="jobExecution.state === 'FAILED'" :clickable="true"
                        v-on:feedback-clicked="openExecutionDetailsDialog(jobExecution)">
                    <div slot="left">
                        <font-awesome-icon icon="spinner" class="fa-spin fa-fw margin-right"
                                           v-if="jobExecution.state === 'RUNNING' || jobExecution.state === 'WAITING'"/>
                        <font-awesome-icon icon="bolt" class="fa-fw" v-if="jobExecution.state === 'FAILED'"/>
                        {{formatJobExecution(jobExecution)}}
                    </div>
                    <div slot="right">
                        <input-button slot="right" icon="times" v-on:clicked="openCancelJobDialog(jobExecution)"
                                      v-if="jobExecution.state === 'WAITING' || jobExecution.state === 'RUNNING'"/>
                        <input-button slot="right" icon="check"
                                      v-on:clicked="openMarkJobExecutionResolvedDialog(jobExecution)"
                                      v-if="jobExecution.state === 'FAILED'"/>
                    </div>
                </feedback-box>
                <list-pager :page="jobExecutionsPage" v-if="jobExecutionsPage.totalPages > 1"
                            v-on:first="manualUpdateJobExecutions(0)"
                            v-on:previous="manualUpdateJobExecutions(jobExecutionsPage.number -1)"
                            v-on:next="manualUpdateJobExecutions(jobExecutionsPage.number + 1)"
                            v-on:last="manualUpdateJobExecutions(jobExecutionsPage.totalPages - 1)"/>
            </core-panel>
        </side-menu>

        <core-content v-if="jobConfiguration">
            <job-configurator
                    v-show="selectedTaskId === null && selectedActionId === null"
                    :job-configuration="jobConfiguration"
                    :validation-errors="validationErrors"
                    v-on:update-original-job-configuration="updateOriginalJobConfiguration()"
                    v-on:open-documentation="openDocumentation"
                    v-on:switch-documentation="switchDocumentation"
                    v-on:close-documentation="showDocumentation = false"
                    ref="jobConfigurator"/>

            <task-configurator v-for="task in jobConfiguration.tasks"
                               v-show="selectedTaskId === task.id"
                               :key="task.id"
                               :task="task"
                               :validation-errors="validationErrors"
                               v-on:create-connector="createConnector"
                               v-on:open-documentation="openDocumentation"
                               v-on:switch-documentation="switchDocumentation"
                               v-on:close-documentation="showDocumentation = false"
                               ref="taskConfigurators"/>

            <template v-for="task in jobConfiguration.tasks">
                <action-configurator v-for="action in task.actions"
                                     v-show="selectedActionId === action.id"
                                     :key="action.id"
                                     :action="action"
                                     :validation-errors="validationErrors"
                                     v-on:create-connector="createConnector"
                                     v-on:open-documentation="openDocumentation"
                                     v-on:switch-documentation="switchDocumentation"
                                     v-on:close-documentation="showDocumentation = false"
                                     ref="actionConfigurators"/>
            </template>

            <job-execution-details v-if="showExecutionDetailsDialog"
                                   v-bind:job-execution="selectedJobExecution"
                                   v-on:close="closeExecutionDetailsDialog()"/>

        </core-content>

        <test-result-container v-if="testResults != null"
                               v-on:close="testResults = null"
                               v-bind:selected-test-results="selectedTestResults"/>

        <modal-dialog v-if="showDeleteTaskDialog" @close="showDeleteTaskDialog = false">
            <h1 slot="header">Delete Task?</h1>
            <div slot="body" class="paragraph">Do you really want to delete this Task?</div>
            <div slot="footer">
                <layout-row>
                    <input-button slot="left" v-on:clicked="showDeleteTaskDialog = false" icon="times"/>
                    <input-button slot="right" v-on:clicked="deleteTask()" icon="check"/>
                </layout-row>
            </div>
        </modal-dialog>

        <modal-dialog v-if="showDeleteActionDialog" @close="showDeleteActionDialog = false">
            <h1 slot="header">Delete Action?</h1>
            <div slot="body" class="paragraph">Do you really want to delete this Action?</div>
            <div slot="footer">
                <layout-row>
                    <input-button slot="left" v-on:clicked="showDeleteActionDialog = false" icon="times"/>
                    <input-button slot="right" v-on:clicked="deleteAction()" icon="check"/>
                </layout-row>
            </div>
        </modal-dialog>

        <modal-dialog v-if="showRunDialog"
                      @close="showRunDialog = false"
                      v-on:cancel="showRunDialog = false">
            <h1 slot="header">Start job</h1>
            <div slot="body" class="paragraph">
                Manually start job now?
            </div>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showRunDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="runJob()" icon="check"/>
            </layout-row>
        </modal-dialog>

        <modal-dialog v-if="showCancelJobDialog"
                      @close="showCancelJobDialog = false"
                      v-on:cancel="showCancelJobDialog = false">
            <h1 slot="header">Cancel job execution</h1>
            <div slot="body" class="paragraph">
                Are you sure you want to cancel this execution?
            </div>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showCancelJobDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="cancelJobExecution()" icon="check"/>
            </layout-row>
        </modal-dialog>

        <modal-dialog v-if="showUnsavedValuesExistDialog" @close="showUnsavedValuesExistDialog = false">
            <h1 slot="header">Unsaved configuration</h1>
            <div slot="body" class="paragraph">There are unsaved configuration changes.<br/><br/>Do you really want to
                leave?
            </div>
            <div slot="footer">
                <layout-row>
                    <input-button slot="left" v-on:clicked="showUnsavedValuesExistDialog = false" icon="times"/>
                    <input-button slot="right" v-on:clicked="nextRoute()" icon="check"/>
                </layout-row>
            </div>
        </modal-dialog>

        <modal-dialog v-if="showMarkJobExecutionResolvedDialog"
                      @close="showMarkJobExecutionResolvedDialog = false"
                      v-on:cancel="showMarkJobExecutionResolvedDialog = false">
            <h1 slot="header">Mark job execution as resolved</h1>
            <div slot="body">
                <div class="paragraph">
                    Mark this failed execution as resolved?
                </div>
                <div class="paragraph" v-if="numFailedExecutionsForSelectedJob > 1">
                    Mark <strong>all {{numFailedExecutionsForSelectedJob}}</strong> executions of this job as resolved:
                    <font-awesome-icon :icon="resolveAllFailedExecutionsOfJob ? 'check-square' : 'square'"
                                       v-on:click="resolveAllFailedExecutionsOfJob = !resolveAllFailedExecutionsOfJob"/>
                </div>
            </div>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showMarkJobExecutionResolvedDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="markJobExecutionResolved()" icon="check"/>
            </layout-row>
        </modal-dialog>

        <documentation-container :documentation="documentation" v-show="showDocumentation"
                                 v-on:close="showDocumentation = false"/>

        <background-icon right="true" icon-one="toolbox"/>

    </core-container>
</template>
<script>
    import CoreContainer from '../components/common/core-container'
    import CoreContent from '../components/common/core-content'
    import JobConfigurator from '../components/jobs/job-configurator'
    import TaskConfigurator from '../components/jobs/task-configurator'
    import ActionConfigurator from '../components/jobs/action-configurator'
    import ModalDialog from '../components/common/modal-dialog'
    import LayoutRow from '../components/common/layout-row'
    import InputButton from '../components/common/input-button'
    import TestResultContainer from '../components/jobs/test-result-container'
    import SideMenu from '../components/common/side-menu'
    import FeedbackBox from '../components/common/feedback-box'
    import JobExecutionDetails from '../components/jobs/job-execution-details'
    import Utils from '../utils/utils.js'
    import BackgroundIcon from '../components/common/background-icon';
    import IgorBackend from '../utils/igor-backend.js'
    import ListPager from "../components/common/list-pager";
    import JobNavigation from "../components/jobs/job-navigation";
    import CorePanel from "../components/common/core-panel";
    import FormatUtils from "../utils/utils";
    import DocumentationContainer from "../components/common/documentation-container";

    export default {
        name: 'job-editor',
        components: {
            DocumentationContainer,
            CorePanel,
            JobNavigation,
            ListPager,
            BackgroundIcon,
            JobExecutionDetails,
            FeedbackBox,
            SideMenu,
            TestResultContainer,
            InputButton,
            LayoutRow,
            ModalDialog,
            ActionConfigurator,
            TaskConfigurator,
            JobConfigurator,
            CoreContent,
            CoreContainer
        },
        props: ['jobId'],
        data: function () {
            return {
                showDeleteTaskDialog: false,
                showDeleteActionDialog: false,
                showCancelJobDialog: false,
                showExecutionDetailsDialog: false,
                showRunDialog: false,
                initialProviderCategory: {},
                initialProviderType: {},
                initialActionCategory: {},
                initialActionType: {},
                selectedTaskId: null,
                selectedActionId: null,
                testResults: null,
                selectedTestResults: null,
                originalJobConfiguration: null,
                jobConfiguration: null,
                validationErrors: {},
                jobExecutionsPage: {
                    number: 0,
                    size: 10,
                    totalPages: 0,
                    items: []
                },
                jobExecutionsRefreshTimer: null,
                selectedJobExecutionListEntry: null,
                selectedJobExecution: null,
                selectedJobExecutionId: null,
                showUnsavedValuesExistDialog: false,
                showMarkJobExecutionResolvedDialog: false,
                showDocumentation: false,
                nextRoute: null,
                resolveAllFailedExecutionsOfJob: false,
                numFailedExecutionsForSelectedJob: 0,
                documentation: null
            }
        },
        computed: {
            jobRunningOrWaiting: function () {
                if (this.jobExecutionsPage) {
                    for (let i = 0; i < this.jobExecutionsPage.items.length; i++) {
                        if ('RUNNING' === this.jobExecutionsPage.items[i].state || 'WAITING' === this.jobExecutionsPage.items[i].state) {
                            return true
                        }
                    }
                }
                return false
            }
        },
        methods: {
            formatJobExecution: function (jobExecution) {
                let formattedState = FormatUtils.capitalize(jobExecution.state.toLowerCase()) + ': ';
                if (jobExecution.state === 'RUNNING' || jobExecution.state === 'WAITING') {
                    formattedState += jobExecution.duration;
                } else {
                    formattedState += FormatUtils.formatInstant(jobExecution.created)
                }
                return formattedState;
            },
            createJob: function () {
                this.jobConfiguration = {
                    id: Utils.uuidv4(),
                    name: 'New Job',
                    trigger: {
                        id: Utils.uuidv4(),
                        category: null,
                        type: null,
                        parameters: []
                    },
                    description: '',
                    historyLimit: 5,
                    active: true,
                    tasks: []
                }
            },
            loadJob: async function (id) {
                this.jobConfiguration = await IgorBackend.getData('/api/job/' + id)
            },
            saveConfiguration: async function () {
                this.testResults = null;
                await IgorBackend.postData('/api/job', this.jobConfiguration, 'Saving job',
                    'Job \'' + Utils.formatNameForSnackbar(this.jobConfiguration.name) + '\' saved.',
                    'Saving failed!').then((result) => {
                    if (result.status === 400) {
                        this.validationErrors = result.data;
                    } else {
                        this.validationErrors = {};
                        this.jobConfiguration = result.data;
                        this.originalJobConfiguration = JSON.stringify(this.jobConfiguration);
                    }
                });
            },
            testConfiguration: async function () {
                this.showDocumentation = false;
                this.testResults = null;
                this.selectedTestResults = null;
                await IgorBackend.postData('/api/job/simulate', this.jobConfiguration, 'Testing job',
                    'Test OK.', 'Test Failed!').then((result) => {
                    if (result.status === 400) {
                        this.validationErrors = result.data;
                    } else {
                        this.validationErrors = {};
                        this.testResults = result.data;
                    }
                });
                this.updateSelectedTestResult()
            },
            cancelConfiguration: function () {
                this.$router.push({name: 'job-overview'})
            },
            selectJob: function () {
                this.selectedTaskId = null;
                this.selectedActionId = null;
                this.showDocumentation = false;
                this.updateSelectedTestResult()
            },
            selectTask: function (taskId) {
                this.selectedTaskId = taskId;
                this.selectedActionId = null;
                this.showDocumentation = false;
                this.updateSelectedTestResult()
            },
            selectAction: function (actionId) {
                this.selectedTaskId = null;
                this.selectedActionId = actionId;
                this.showDocumentation = false;
                this.updateSelectedTestResult()
            },
            addTask: function () {
                let task = {
                    id: Utils.uuidv4(),
                    name: 'New Task',
                    description: '',
                    active: true,
                    simulationLimit: 25,
                    provider: {
                        id: Utils.uuidv4(),
                        category: this.initialProviderCategory,
                        type: this.initialProviderType,
                        parameters: []
                    },
                    actions: []
                };
                this.jobConfiguration.tasks.push(task);
                this.selectTask(task.id)
            },
            duplicateTask: function (taskId) {
                let originalTask = Utils.findTask(this.jobConfiguration, taskId);
                let copiedTask = JSON.parse(JSON.stringify(originalTask));
                copiedTask.id = Utils.uuidv4();
                copiedTask.name = "Copy of " + copiedTask.name;
                delete copiedTask.provider.id;
                copiedTask.actions.forEach((action) => {
                    action.id = Utils.uuidv4();
                });
                this.$set(this.jobConfiguration.tasks, this.jobConfiguration.tasks.length, copiedTask);
                this.selectTask(copiedTask.id)
            },
            showDeleteTask: function (taskId) {
                this.selectedTaskId = taskId;
                this.showDeleteTaskDialog = true
            },
            deleteTask: function () {
                this.validationErrors = {};
                this.$delete(this.jobConfiguration.tasks, Utils.findTaskIndex(this.jobConfiguration, this.selectedTaskId));
                this.showDeleteTaskDialog = false;
                this.selectedTaskId = null;
                this.selectedActionId = null;
                this.testResults = null
            },
            addAction: function (taskId) {
                let action = {
                    id: Utils.uuidv4(),
                    active: true,
                    name: '',
                    category: this.initialActionCategory,
                    type: this.initialActionType,
                    parameters: []
                };
                Utils.findTask(this.jobConfiguration, taskId).actions.push(action);
                this.selectAction(action.id)
            },
            showDeleteAction: function (actionId) {
                this.selectedActionId = actionId;
                this.showDeleteActionDialog = true
            },
            deleteAction: function () {
                this.validationErrors = {};
                let task = Utils.findTaskWithAction(this.jobConfiguration, this.selectedActionId);
                this.$delete(task.actions, Utils.findActionIndex(task, this.selectedActionId));
                this.showDeleteActionDialog = false;
                this.selectedTaskId = null;
                this.selectedActionId = null;
                this.testResults = null;
            },
            updateSelectedTestResult: function () {
                if (this.testResults != null) {
                    let taskId = this.selectedTaskId;
                    let actionId = this.selectedActionId;

                    // The job has been selected:
                    if (taskId === null && actionId === null) {
                        if (this.testResults['job-result'] && this.testResults['job-result'].errorCause) {
                            this.selectedTestResults = this.testResults['job-result']
                        } else {
                            this.selectedTestResults = null
                        }
                        return
                    }

                    // A Task has been selected:
                    if (taskId != null && this.testResults[taskId] != null) {
                        this.selectedTestResults = this.testResults[taskId]
                    } else if (actionId != null && this.testResults[actionId] != null) {
                        // An action has been selected:
                        this.selectedTestResults = this.testResults[actionId];
                    } else {
                        this.selectedTestResults = null;
                    }
                }
            },
            runJob: async function () {
                this.showRunDialog = false;
                    IgorBackend.postData('/api/job/run', this.jobConfiguration, 'Starting job', 'Job \'' +
                        Utils.formatNameForSnackbar(this.jobConfiguration.name) + '\' started manually.', 'Job \'' +
                        Utils.formatNameForSnackbar(this.jobConfiguration.name) + '\' startup failed!').then((result) => {
                        if (result.status === 400) {
                            this.validationErrors = result.data;
                        } else {
                            this.validationErrors = {};
                            this.jobConfiguration = result.data;
                        }
                    })
            },
            updateJobExecutions: async function () {
                if (this.jobConfiguration.id) {
                    this.jobExecutionsPage = await IgorBackend.getData('/api/execution/job/' + this.jobConfiguration.id + '?pageNumber=' +
                        this.jobExecutionsPage.number + '&pageSize=' + this.jobExecutionsPage.size)
                }
            },
            manualUpdateJobExecutions: async function (page) {
                if (this.jobConfiguration.id) {
                    clearInterval(this.jobExecutionsRefreshTimer);
                    this.jobExecutionsPage = await IgorBackend.getData('/api/execution/job/' + this.jobConfiguration.id + '?pageNumber=' +
                        page + '&pageSize=' + this.jobExecutionsPage.size);
                    this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
                }
            },
            openExecutionDetailsDialog: async function (selectedJobExecutionListEntry) {
                this.selectedJobExecutionId = selectedJobExecutionListEntry.id;
                this.selectedJobExecution = await IgorBackend.getData('/api/execution/details/' + this.selectedJobExecutionId);
                this.showExecutionDetailsDialog = true;
                if (this.selectedJobExecution.executionState === 'WAITING' || this.selectedJobExecution.executionState === 'RUNNING') {
                    this.jobExecutionDetailsRefreshTimer = setInterval(() => {
                        this.updateJobExectuionDetails()
                    }, 1000)
                }
            },
            closeExecutionDetailsDialog: function () {
                if (this.jobExecutionDetailsRefreshTimer) {
                    clearTimeout(this.jobExecutionDetailsRefreshTimer)
                }
                this.showExecutionDetailsDialog = false
            },
            updateJobExectuionDetails: async function () {
                this.selectedJobExecution = await IgorBackend.getData('/api/execution/details/' + this.selectedJobExecutionId)
            },
            openCancelJobDialog: function (selectedJobExecutionListEntry) {
                this.selectedJobExecutionListEntry = selectedJobExecutionListEntry;
                this.showCancelJobDialog = true
            },
            cancelJobExecution: function () {
                clearInterval(this.jobExecutionsRefreshTimer);
                this.showCancelJobDialog = false;
                IgorBackend.postData('/api/execution/' + this.selectedJobExecutionListEntry.id + '/cancel', null,
                    "Cancelling job", "Job cancelled.", "Job could not be cancelled!").then(() => {
                    this.updateJobExecutions().then(() => {
                        this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
                    })
                })
            },
            createConnector: function (selectionKey, parameterIndex, connectorCategoryCandidates) {
                this.$root.$data.store.setJobData(this.jobConfiguration, selectionKey, parameterIndex, connectorCategoryCandidates);
                this.$router.push({name: 'connector-editor'})
            },
            updateOriginalJobConfiguration: function () {
                this.originalJobConfiguration = JSON.stringify(this.jobConfiguration)
            },
            openMarkJobExecutionResolvedDialog: async function (selectedJobExecutionListEntry) {
                this.selectedJobExecutionListEntry = selectedJobExecutionListEntry;
                this.$root.$data.store.setWip('Loading job execution details...');
                this.numFailedExecutionsForSelectedJob = await IgorBackend.getData('/api/execution/job/' +
                    this.selectedJobExecutionListEntry.jobId + '/FAILED/count');
                this.$root.$data.store.clearWip();
                this.showMarkJobExecutionResolvedDialog = true
            },
            markJobExecutionResolved: async function () {
                clearTimeout(this.jobExecutionsRefreshTimer);
                await IgorBackend.putData('/api/execution/' + this.selectedJobExecutionListEntry.id + '/' +
                    this.selectedJobExecutionListEntry.jobId + '/FAILED/RESOLVED?updateAllOfJob=' + this.resolveAllFailedExecutionsOfJob, null,
                    'Updating executions', 'Executions updated', 'Executions could not be updated!');
                await this.manualUpdateJobExecutions(0);
                this.resolveAllFailedExecutionsOfJob = false;
                this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000);
                this.showMarkJobExecutionResolvedDialog = false
            },
            openDocumentation: async function (filename) {
                this.documentation = await IgorBackend.getData('/api/doc/' + filename);
                this.showDocumentation = true;
                this.testResults = null;
            },
            switchDocumentation: async function (filename) {
                if (this.showDocumentation) {
                    this.documentation = await IgorBackend.getData('/api/doc/' + filename);
                    this.testResults = null;
                }
            }
        },
        mounted() {
            let jobData = this.$root.$data.store.getJobData();
            // Returning from a connector configuration within a job configuration
            if (jobData.jobConfiguration != null) {
                this.jobConfiguration = jobData.jobConfiguration;

                let selectionKey = jobData.selectionKey;
                if (selectionKey != null) {

                    let task = Utils.findTask(this.jobConfiguration, selectionKey);
                    if (task) {
                        if (jobData.connectorParameter != null && jobData.parameterIndex != null) {
                            let parameter = task.provider.parameters[jobData.parameterIndex];
                            parameter.connectorName = jobData.connectorParameter.name;
                            parameter.value = jobData.connectorParameter.id
                        }
                        this.selectTask(task.id);
                    } else {
                        let action = Utils.findAction(this.jobConfiguration, selectionKey);
                        if (action) {
                            if (jobData.connectorParameter != null && jobData.parameterIndex != null) {
                                let parameter = action.parameters[jobData.parameterIndex];
                                parameter.connectorName = jobData.connectorParameter.name;
                                parameter.value = jobData.connectorParameter.id
                            }
                            this.selectAction(action.id);
                        }
                    }
                }
                this.updateJobExecutions().then(() => {
                    this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
                });
                this.originalJobConfiguration = JSON.stringify(this.jobConfiguration);
                this.$root.$data.store.clearJobData()
            } else if (this.jobId != null) {
                this.loadJob(this.jobId).then(() => {
                    this.updateJobExecutions().then(() => {
                        this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
                    });
                    this.originalJobConfiguration = JSON.stringify(this.jobConfiguration)
                })
            } else {
                // The job-configurator loads trigger data and modifies the initial jobConfiguration. So the 'originalJobConfiguration'
                // property is set there, and 'update-original-job-configuration' is emitted...
                this.createJob();
                this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
            }

            let component = this;
            IgorBackend.getData('/api/category/provider').then((categoryResult) => {
                this.initialProviderCategory = Array.from(categoryResult)[0];
                IgorBackend.getData('/api/type/' + component.initialProviderCategory.key).then((typeResult) => {
                    component.initialProviderType = Array.from(typeResult)[0]
                })
            });
            IgorBackend.getData('/api/category/action').then((categoryResult) => {
                this.initialActionCategory = Array.from(categoryResult)[0];
                IgorBackend.getData('/api/type/' + component.initialActionCategory.key).then((typeResult) => {
                    component.initialActionType = Array.from(typeResult)[0]
                })
            })
        },
        destroyed() {
            clearInterval(this.jobExecutionsRefreshTimer);
            clearInterval(this.jobExecutionDetailsRefreshTimer)
        },
        beforeRouteLeave(to, from, next) {
            // We leave the job editor to create a new connector. No unsaved-values-check required!
            let jobData = this.$root.$data.store.getJobData();
            if (jobData.jobConfiguration) {
                next()
            } else {
                if (this.originalJobConfiguration) {
                    let newJobConfiguration = JSON.stringify(this.jobConfiguration);
                    if (this.originalJobConfiguration !== newJobConfiguration) {
                        this.nextRoute = next;
                        this.showUnsavedValuesExistDialog = true;
                        return
                    }
                }
                next();
            }
        }
    }
</script>


<style scoped>

    .paragraph {
        margin-bottom: 2em;
    }

</style>
