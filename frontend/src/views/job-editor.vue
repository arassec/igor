<template>
    <core-container>

        <side-menu v-if="jobConfiguration" data-e2e="job-editor-navigator">
            <div slot="content" class="sticky">
                <transition v-on:after-leave="blendInExecutions"
                            name="animate-css-transition"
                            enter-active-class="animated slideInLeft"
                            leave-active-class="animated slideOutLeft">
                    <job-navigation v-if="showConfiguration"
                                    :job-configuration="jobConfiguration"
                                    :selected-action-id="selectedActionId"
                                    :job-running-or-waiting="jobRunningOrWaiting"
                                    :validation-errors="validationErrors"
                                    :job-executions-page="jobExecutionsPage"
                                    :simulation-results="simulationResults"
                                    v-on:cancel-configuration="cancelConfiguration"
                                    v-on:test-configuration="simulateJob"
                                    v-on:save-configuration="saveConfiguration"
                                    v-on:show-executions="showConfiguration = false"
                                    v-on:run-job="showRunDialog = true"
                                    v-on:job-is-selected="selectJob"
                                    v-on:action-is-selected="selectAction"
                                    v-on:add-action="addAction"
                                    v-on:delete-action="showDeleteAction"/>
                </transition>
            </div>
            <div slot="content" class="sticky">
                <transition v-on:after-leave="blendInConfiguration"
                            name="animate-css-transition"
                            enter-active-class="animated slideInLeft"
                            leave-active-class="animated slideOutLeft">
                    <div v-if="showExecutions" data-e2e="job-editor-executions">
                        <core-panel class="executions-top-panel">
                            <h1>
                                <font-awesome-icon icon="tasks" class="margin-right fa-fw"/>
                                Job Executions
                            </h1>
                            <layout-row>
                                <input-button slot="left" icon="chevron-left" v-on:clicked="showExecutions = false"
                                              data-e2e="show-job-configuration-button"/>
                                <input-button slot="left" icon="save" v-on:clicked="saveConfiguration"
                                              class="margin-left"
                                              data-e2e="save-job-button"/>
                            </layout-row>
                        </core-panel>
                        <core-panel v-if="showExecutions && jobExecutionsPage.items.length > 0">
                            <feedback-box
                                v-for="(jobExecution, index) in jobExecutionsPage.items"
                                :key="index" :alert="jobExecution.state === 'FAILED'" :clickable="true"
                                v-on:feedback-clicked="openExecutionDetailsDialog(jobExecution)"
                                :data-e2e="'job-execution-' + index">
                                <div slot="left">
                                    <font-awesome-icon icon="circle-notch" class="fa-spin fa-fw"
                                                       v-if="jobExecution.state === 'RUNNING'"/>
                                    <font-awesome-icon icon="sign-in-alt" class="fa-fw"
                                                       v-if="jobExecution.state === 'ACTIVE'"/>
                                    <font-awesome-icon icon="hourglass" class="fa-fw fal"
                                                       v-if="jobExecution.state === 'WAITING'"/>
                                    <font-awesome-icon icon="bolt" class="fa-fw"
                                                       v-if="jobExecution.state === 'FAILED'"/>
                                    {{ formatJobExecution(jobExecution) }}
                                </div>
                                <div slot="right">
                                    <input-button icon="times"
                                                  v-on:clicked="openCancelJobDialog(jobExecution)"
                                                  v-if="jobExecution.state === 'WAITING' || jobExecution.state === 'RUNNING' || jobExecution.state === 'ACTIVE'"/>
                                    <input-button icon="check"
                                                  v-on:clicked="openMarkJobExecutionResolvedDialog(jobExecution)"
                                                  v-if="jobExecution.state === 'FAILED'"
                                                  :data-e2e="'job-execution-' + index + '-mark-resolved-button'"/>
                                </div>
                            </feedback-box>
                        </core-panel>
                        <list-pager :page="jobExecutionsPage" v-if="jobExecutionsPage.totalPages > 1"
                                    v-on:first="manualUpdateJobExecutions(0)"
                                    v-on:previous="manualUpdateJobExecutions(jobExecutionsPage.number -1)"
                                    v-on:next="manualUpdateJobExecutions(jobExecutionsPage.number + 1)"
                                    v-on:last="manualUpdateJobExecutions(jobExecutionsPage.totalPages - 1)"/>
                    </div>
                </transition>
            </div>
        </side-menu>

        <core-content class="configurator fixed-width" v-if="jobConfiguration">
            <job-configurator
                v-show="selectedActionId === null"
                :job-configuration="jobConfiguration"
                :validation-errors="validationErrors"
                v-on:create-connector="createConnector"
                v-on:connector-selected="connectorSelectedForTrigger"
                v-on:set-cron-expression="setCronExpressionForTrigger"
                v-on:open-documentation="openDocumentation"
                v-on:switch-documentation="switchDocumentation"
                v-on:close-documentation="closeDocumentation"
                v-on:toggle-job-active="toggleJobActive"
                v-on:change-job-name="changeJobName"
                v-on:change-job-description="changeJobDescription"
                v-on:change-job-history-limit="changeJobHistoryLimit"
                v-on:change-job-simulation-limit="changeJobSimulationLimit"
                v-on:toggle-job-fault-tolerant="toggleJobFaultTolerant"
                v-on:change-job-num-threads="changeJobNumThreads"
                v-on:change-job-trigger-category="changeJobTriggerCategory"
                v-on:change-job-trigger-type="changeJobTriggerType"
                v-on:change-job-trigger-parameters="changeJobTriggerParameters"
                ref="jobConfigurator"/>

            <action-configurator v-for="action in jobConfiguration.actions"
                                 v-show="selectedActionId === action.id"
                                 :key="action.id"
                                 :action="action"
                                 :validation-errors="validationErrors"
                                 :event-trigger="jobConfiguration.trigger.type.supportsEvents"
                                 v-on:create-connector="createConnector"
                                 v-on:connector-selected="connectorSelectedForAction"
                                 v-on:set-cron-expression="setCronExpressionForAction"
                                 v-on:open-documentation="openDocumentation"
                                 v-on:switch-documentation="switchDocumentation"
                                 v-on:close-documentation="closeDocumentation"
                                 v-on:toggle-action-active="toggleActionActive"
                                 v-on:change-action-name="changeActionName"
                                 v-on:change-action-description="changeActionDescription"
                                 v-on:change-action-category="changeActionCategory"
                                 v-on:change-action-type="changeActionType"
                                 v-on:change-action-parameters="changeActionParameters"
                                 ref="actionConfigurators"/>
        </core-content>

        <transition v-on:after-leave="blendInDocumentation"
                    name="animate-css-transition"
                    enter-active-class="animated slideInRight"
                    leave-active-class="animated slideOutRight">
            <simulation-result-container v-show="showSimulationResults && simulationResults != null"
                                         v-on:close="closeSimulationResults"
                                         v-bind:selected-simulation-results="selectedSimulationResults"/>
        </transition>

        <transition v-on:after-leave="blendInSimulationResults"
                    name="animate-css-transition"
                    enter-active-class="animated slideInRight"
                    leave-active-class="animated slideOutRight">
            <documentation-container :documentation="documentation" v-show="showDocumentation"
                                     v-on:close="closeDocumentation"/>
        </transition>

        <job-execution-details v-if="selectedJobExecution"
                               v-show="showExecutionDetailsDialog"
                               v-bind:job-execution="selectedJobExecution"
                               v-on:close="closeExecutionDetailsDialog()"/>

        <modal-dialog v-show="showDeleteActionDialog" @close="showDeleteActionDialog = false">
            <h1 slot="header">Delete Action?</h1>
            <div slot="body" class="paragraph">Do you really want to delete this Action?</div>
            <div slot="footer">
                <layout-row>
                    <input-button slot="left" v-on:clicked="showDeleteActionDialog = false" icon="times"/>
                    <input-button slot="right" v-on:clicked="deleteAction()" icon="check"/>
                </layout-row>
            </div>
        </modal-dialog>

        <modal-dialog v-show="showRunDialog"
                      @close="showRunDialog = false"
                      v-on:cancel="showRunDialog = false">
            <h1 slot="header">Start job</h1>
            <div slot="body" class="paragraph">
                Manually start job now?
            </div>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showRunDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="runJob()" icon="check" data-e2e="run-job-confirm-button"/>
            </layout-row>
        </modal-dialog>

        <modal-dialog v-show="showCancelJobDialog"
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

        <modal-dialog v-show="showUnsavedValuesExistDialog" @close="showUnsavedValuesExistDialog = false">
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

        <modal-dialog v-show="showMarkJobExecutionResolvedDialog"
                      @close="showMarkJobExecutionResolvedDialog = false"
                      v-on:cancel="showMarkJobExecutionResolvedDialog = false">
            <h1 slot="header">Mark job execution as resolved</h1>
            <div slot="body">
                <div class="paragraph">
                    Mark this failed execution as resolved?
                </div>
                <div class="paragraph" v-if="numFailedExecutionsForSelectedJob > 1">
                    Mark <strong>all {{ numFailedExecutionsForSelectedJob }}</strong> executions of this job as
                    resolved:
                    <font-awesome-icon :icon="resolveAllFailedExecutionsOfJob ? 'check-square' : 'square'"
                                       v-on:click="resolveAllFailedExecutionsOfJob = !resolveAllFailedExecutionsOfJob"/>
                </div>
            </div>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showMarkJobExecutionResolvedDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="markJobExecutionResolved()" icon="check"/>
            </layout-row>
        </modal-dialog>

        <background-icon icon="tools"/>

    </core-container>
</template>
<script>
import CoreContainer from '../components/common/core-container'
import CoreContent from '../components/common/core-content'
import JobConfigurator from '../components/jobs/job-configurator'
import ActionConfigurator from '../components/jobs/action-configurator'
import ModalDialog from '../components/common/modal-dialog'
import LayoutRow from '../components/common/layout-row'
import InputButton from '../components/common/input-button'
import SimulationResultContainer from '../components/jobs/simulation-result-container'
import SideMenu from '../components/common/side-menu'
import FeedbackBox from '../components/common/feedback-box'
import JobExecutionDetails from '../components/jobs/job-execution-details'
import Utils from '../utils/utils.js'
import IgorBackend from '../utils/igor-backend.js'
import ListPager from "../components/common/list-pager";
import JobNavigation from "../components/jobs/job-navigation";
import CorePanel from "../components/common/core-panel";
import FormatUtils from "../utils/utils";
import DocumentationContainer from "../components/common/documentation-container";
import BackgroundIcon from "@/components/common/background-icon";
import Vue from "vue";

export default {
    name: 'job-editor',
    components: {
        BackgroundIcon,
        DocumentationContainer,
        CorePanel,
        JobNavigation,
        ListPager,
        JobExecutionDetails,
        FeedbackBox,
        SideMenu,
        SimulationResultContainer,
        InputButton,
        LayoutRow,
        ModalDialog,
        ActionConfigurator,
        JobConfigurator,
        CoreContent,
        CoreContainer
    },
    props: ['jobId'],
    data: function () {
        return {
            showDeleteActionDialog: false,
            showCancelJobDialog: false,
            showExecutionDetailsDialog: false,
            showRunDialog: false,
            showExecutions: false,
            showConfiguration: true,
            showSimulationResults: false,
            shouldShowSimulationResults: false,
            showDocumentation: false,
            shouldShowDocumentation: false,
            selectedActionId: null,
            simulationResults: null,
            selectedSimulationResults: null,
            originalJobConfiguration: null,
            jobConfiguration: null,
            validationErrors: {},
            jobExecutionsPage: {
                number: 0,
                size: 10,
                totalPages: 0,
                items: []
            },
            selectedJobExecutionListEntry: null,
            selectedJobExecution: null,
            selectedJobExecutionId: null,
            showUnsavedValuesExistDialog: false,
            showMarkJobExecutionResolvedDialog: false,
            nextRoute: null,
            resolveAllFailedExecutionsOfJob: false,
            numFailedExecutionsForSelectedJob: 0,
            documentation: null,
            jobListEventSource: null,
            jobExecutionEventSource: null,
        }
    },
    computed: {
        jobRunningOrWaiting: function () {
            if (this.jobExecutionsPage) {
                for (const element of this.jobExecutionsPage.items) {
                    if ('RUNNING' === element.state
                        || 'WAITING' === element.state
                        || 'ACTIVE' === element.state) {
                        return true
                    }
                }
            }
            return false
        }
    },
    methods: {
        blendInExecutions: function () {
            this.showExecutions = true;
        },
        blendInConfiguration: function () {
            this.showConfiguration = true;
        },
        blendInSimulationResults: function () {
            if (this.shouldShowSimulationResults) {
                this.showSimulationResults = true;
            }
        },
        blendInDocumentation: function () {
            if (this.shouldShowDocumentation) {
                this.showDocumentation = true;
            }
        },
        formatJobExecution: function (jobExecution) {
            let formattedState = FormatUtils.capitalize(jobExecution.state.toLowerCase()) + ': ';
            if (jobExecution.state === 'RUNNING' || jobExecution.state === 'WAITING') {
                formattedState += jobExecution.duration;
            } else {
                formattedState += FormatUtils.formatInstant(jobExecution.created)
            }
            return formattedState;
        },
        loadJob: async function (id) {
            this.jobConfiguration = await IgorBackend.getData('/api/job/' + id)
        },
        saveConfiguration: async function () {
            await IgorBackend.postData('/api/job', this.jobConfiguration, 'Saving job',
                'Job \'' + Utils.formatNameForSnackbar(this.jobConfiguration.name) + '\' saved.',
                'Saving failed!').then((result) => {
                if (result.status === 400) {
                    if (result.data === undefined || result.data === "") {
                        this.validationErrors = {};
                    } else {
                        this.validationErrors = result.data;
                    }
                } else {
                    this.validationErrors = {};
                    this.jobConfiguration = result.data;
                    this.originalJobConfiguration = JSON.stringify(this.jobConfiguration);
                }
            });
        },
        simulateJob: async function () {
            this.showDocumentation = false;
            this.simulationResults = null;
            this.selectedSimulationResults = null;
            let message = 'Simulating job';
            if (this.jobConfiguration.trigger.type.supportsEvents) {
                message = 'Simulating job - Waiting for incoming events...'
            }
            let component = this;
            const CancelToken = window.axios.CancelToken;
            const source = CancelToken.source();
            await IgorBackend.postData('/api/job/simulate', this.jobConfiguration, message,
                'Simulation OK.', 'Simulation Failed!', () => {
                    component.showDocumentation = false;
                    source.cancel("Simulation cancelled by user!");
                    IgorBackend.deleteData('/api/job/simulate/' + component.jobConfiguration.id, "Cancelling simulation...", "Simulation canceled.", "Cancelling failed!");
                }, source.token).then((result) => {
                if (result.status === 400) {
                    component.showDocumentation = false;
                    if (result.data === undefined || result.data === "") {
                        this.validationErrors = {};
                    } else {
                        this.validationErrors = result.data;
                    }
                } else {
                    this.validationErrors = {};
                    this.simulationResults = result.data;
                }
                this.showSimulationResults = true;
            });
            this.updateSelectedSimulationResult()
        },
        cancelConfiguration: function () {
            this.$router.push({name: 'job-overview'})
        },
        selectJob: function () {
            this.selectedActionId = null;
            this.showDocumentation = false;
            this.updateSelectedSimulationResult()
        },
        selectAction: function (actionId) {
            this.selectedActionId = actionId;
            this.showDocumentation = false;
            this.updateSelectedSimulationResult()
        },
        addAction: async function () {
            let action = await IgorBackend.getData('/api/job/action/prototype');
            if (this.simulationResults) {
                let staleSimulationResult = JSON.parse(
                    JSON.stringify(this.simulationResults[this.jobConfiguration.actions[this.jobConfiguration.actions.length - 1].id]));
                staleSimulationResult['stale'] = true;
                this.simulationResults[action.id] = staleSimulationResult;
            }
            this.jobConfiguration.actions.push(action);
            this.selectAction(action.id)
        },
        showDeleteAction: function (actionId) {
            this.selectedActionId = actionId;
            this.showDeleteActionDialog = true
        },
        deleteAction: function () {
            this.validationErrors = {};
            this.$delete(this.jobConfiguration.actions, Utils.findActionIndex(this.jobConfiguration, this.selectedActionId));
            this.showDeleteActionDialog = false;
            this.selectedActionId = null;
            this.simulationResults = null;
        },
        updateSelectedSimulationResult: function () {
            if (this.simulationResults != null) {
                let actionId = this.selectedActionId;

                // The job has been selected, use the trigger's test data:
                if (actionId === null) {
                    if (this.simulationResults[this.jobConfiguration.id]) {
                        this.selectedSimulationResults = this.simulationResults[this.jobConfiguration.id]
                    } else {
                        this.selectedSimulationResults = null;
                    }
                    return
                }

                // An Action has been selected:
                if (actionId != null && this.simulationResults[actionId] != null) {
                    this.selectedSimulationResults = this.simulationResults[actionId];
                } else {
                    this.selectedSimulationResults = null;
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
                    this.showConfiguration = false;
                    this.originalJobConfiguration = JSON.stringify(this.jobConfiguration);
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
                this.jobExecutionsPage = await IgorBackend.getData('/api/execution/job/' + this.jobConfiguration.id + '?pageNumber=' +
                    page + '&pageSize=' + this.jobExecutionsPage.size);
            }
        },
        openExecutionDetailsDialog: async function (selectedJobExecutionListEntry) {
            this.selectedJobExecutionId = selectedJobExecutionListEntry.id;
            this.selectedJobExecution = await IgorBackend.getData('/api/execution/details/' + this.selectedJobExecutionId);
            this.showExecutionDetailsDialog = true;
            if (this.selectedJobExecution.executionState === 'WAITING' || this.selectedJobExecution.executionState === 'RUNNING') {
                this.initJobExecutionEventSource();
            }
        },
        closeExecutionDetailsDialog: function () {
            if (this.jobExecutionEventSource) {
                this.jobExecutionEventSource.close();
            }
            this.showExecutionDetailsDialog = false
        },
        openCancelJobDialog: function (selectedJobExecutionListEntry) {
            this.selectedJobExecutionListEntry = selectedJobExecutionListEntry;
            this.showCancelJobDialog = true
        },
        cancelJobExecution: function () {
            this.showCancelJobDialog = false;
            IgorBackend.postData('/api/execution/' + this.selectedJobExecutionListEntry.id + '/cancel', null,
                "Cancelling job", "Job cancelled.", "Job could not be cancelled!")
        },
        createConnector: function (selectionKey, parameterIndex, connectorCategoryCandidates) {
            this.$root.$data.store.setJobData(this.jobConfiguration, selectionKey, parameterIndex, connectorCategoryCandidates);
            this.$router.push({name: 'connector-editor'})
        },
        getSelectedActionIndex: function () {
            for (let i = 0; i < this.jobConfiguration.actions.length; i++) {
                if (this.jobConfiguration.actions[i].id === this.selectedActionId) {
                    return i;
                }
            }
            return -1;
        },
        connectorSelectedForAction: function (connector, connectorParameterIndex) {
            this.jobConfiguration.actions[this.getSelectedActionIndex()].parameters[connectorParameterIndex].connectorName = connector.name;
            this.jobConfiguration.actions[this.getSelectedActionIndex()].parameters[connectorParameterIndex].value = connector.id;
        },
        setCronExpressionForAction: function (cronExpression, cronParameterIndex) {
            this.jobConfiguration.actions[this.getSelectedActionIndex()].parameters[cronParameterIndex].value = cronExpression
        },
        connectorSelectedForTrigger: function (connector, connectorParameterIndex) {
            this.jobConfiguration.trigger.parameters[connectorParameterIndex].connectorName = connector.name;
            this.jobConfiguration.trigger.parameters[connectorParameterIndex].value = connector.id;
        },
        setCronExpressionForTrigger: function (cronExpression, cronParameterIndex) {
            this.jobConfiguration.trigger.parameters[cronParameterIndex].value = cronExpression
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
            await IgorBackend.putData('/api/execution/' + this.selectedJobExecutionListEntry.id + '/' +
                this.selectedJobExecutionListEntry.jobId + '/FAILED/RESOLVED?updateAllOfJob=' + this.resolveAllFailedExecutionsOfJob, null,
                'Updating executions', 'Executions updated', 'Executions could not be updated!');
            this.resolveAllFailedExecutionsOfJob = false;
            this.showMarkJobExecutionResolvedDialog = false
        },
        openDocumentation: async function (filename) {
            this.documentation = await IgorBackend.getData('/api/doc/' + filename);
            if (this.showSimulationResults) {
                this.showSimulationResults = false;
                this.shouldShowDocumentation = true;
            } else {
                this.showDocumentation = true;
            }
        },
        switchDocumentation: async function (filename) {
            if (this.showDocumentation) {
                this.documentation = await IgorBackend.getData('/api/doc/' + filename);
            }
        },
        closeDocumentation: function () {
            if (this.simulationResults) {
                this.shouldShowSimulationResults = true;
                this.shouldShowDocumentation = false;
                this.showDocumentation = false;
            } else {
                this.showDocumentation = false;
                this.shouldShowDocumentation = false;
            }
        },
        closeSimulationResults: function () {
            if (this.simulationResults) {
                this.simulationResults = null;
                this.shouldShowSimulationResults = false;
                this.showSimulationResults = false;
            }
        },
        initJobListEventSource: function () {
            console.log("AAAAAAAAAAAAAAAAA: " + JSON.stringify(this.jobListEventSource))
            if (this.jobListEventSource) {
                console.log("BBBBBBBBBBBBBB")
                this.jobListEventSource.close();
            }
            this.jobListEventSource = new EventSource('api/job/stream');

            this.jobListEventSource.onmessage = function (event) {
                console.log("EVENT: " + JSON.stringify(event));
            };

            let component = this;
            this.jobListEventSource.onopen = function () {
                console.log('connection is established');
            };

            this.jobListEventSource.addEventListener('error', (event) => {
                console.log("BAAAAD: " + JSON.stringify(event));
            })
            this.jobListEventSource.addEventListener('state-update', function (event) {
                let jobListEntry = JSON.parse(event.data);
                if (jobListEntry.id === component.jobConfiguration.id) {
                    let elementIndex = -1;
                    component.jobExecutionsPage.items.forEach(function (item, index) {
                        if (item.id === jobListEntry.execution.id) {
                            elementIndex = index;
                        }
                    })
                    if (elementIndex !== -1) {
                        if (jobListEntry.execution.state === 'WAITING' || jobListEntry.execution.state === 'RUNNING') {
                            component.$set(component.jobExecutionsPage.items, elementIndex, jobListEntry.execution);
                        } else {
                            component.updateJobExecutions();
                        }
                    } else {
                        // Put new entry on top of the list:
                        component.jobExecutionsPage.items.unshift(jobListEntry.execution);
                        if (component.jobExecutionsPage.items.size > component.jobExecutionsPage.size) {
                            component.jobExecutionsPage.items.pop();
                        }
                    }
                }
            }, false);
            this.jobListEventSource.onerror = () => {
                console.log("BBBBBBBBBBBBBBAAAAAA")
                setTimeout(this.initJobListEventSource, 5000);
            }
        },
        initJobExecutionEventSource: function () {
            console.log("CCCCCCCCCCCCCCCCCCCCCCc")
            this.jobExecutionEventSource = new EventSource('api/execution/stream');
            let component = this;
            this.jobExecutionEventSource.onmessage = event => {
                let jobExecutionDetails = JSON.parse(event.data)
                if (jobExecutionDetails.id === component.selectedJobExecutionId) {
                    component.selectedJobExecution = jobExecutionDetails;
                }
            }
            this.jobExecutionEventSource.onerror = () => {
                setTimeout(this.initJobListEventSource, 5000);
            }
        },
        toggleActionActive: function () {
            this.jobConfiguration.actions[this.getSelectedActionIndex()].active =
                !this.jobConfiguration.actions[this.getSelectedActionIndex()].active;
        },
        changeActionName: function (name) {
            this.jobConfiguration.actions[this.getSelectedActionIndex()].name = name;
        },
        changeActionDescription: function (description) {
            this.jobConfiguration.actions[this.getSelectedActionIndex()].description = description;
        },
        changeActionCategory: function (category) {
            this.jobConfiguration.actions[this.getSelectedActionIndex()].category = category;
        },
        changeActionType: function (type) {
            this.jobConfiguration.actions[this.getSelectedActionIndex()].type = type;
        },
        changeActionParameters: function (parameters) {
            this.jobConfiguration.actions[this.getSelectedActionIndex()].parameters = parameters;
        },
        toggleJobActive: function () {
            this.jobConfiguration.active = !this.jobConfiguration.active;
        },
        changeJobName: function (name) {
            this.jobConfiguration.name = name;
        },
        changeJobDescription: function (description) {
            this.jobConfiguration.description = description;
        },
        changeJobHistoryLimit: function (limit) {
            this.jobConfiguration.historyLimit = limit;
        },
        changeJobSimulationLimit: function (limit) {
            this.jobConfiguration.simulationLimit = limit;
        },
        toggleJobFaultTolerant: function () {
            this.jobConfiguration.faultTolerant = !this.jobConfiguration.faultTolerant;
        },
        changeJobNumThreads: function (numThreads) {
            this.jobConfiguration.numThreads = numThreads;
        },
        changeJobTriggerCategory: function (category) {
            this.jobConfiguration.trigger.category = category;
        },
        changeJobTriggerType: function (type) {
            this.jobConfiguration.trigger.type = type;
        },
        changeJobTriggerParameters: function (parameters) {
            this.jobConfiguration.trigger.parameters = parameters;
        }
    },
    async mounted() {
        let jobData = this.$root.$data.store.getJobData();
        // Returning from a connector configuration within a job configuration
        if (jobData.jobConfiguration != null) {
            this.jobConfiguration = jobData.jobConfiguration;
            let selectionKey = jobData.selectionKey;
            if (selectionKey != null) {
                let action = Utils.findAction(this.jobConfiguration, selectionKey);
                if (action) {
                    if (jobData.connectorParameter != null && jobData.parameterIndex != null) {
                        let parameter = action.parameters[jobData.parameterIndex];
                        parameter.connectorName = jobData.connectorParameter.name;
                        parameter.value = jobData.connectorParameter.id
                    }
                    this.selectAction(action.id);
                } else {
                    if (jobData.connectorParameter != null && jobData.parameterIndex != null) {
                        let parameter = this.jobConfiguration.trigger.parameters[jobData.parameterIndex];
                        parameter.connectorName = jobData.connectorParameter.name;
                        parameter.value = jobData.connectorParameter.id
                    }
                }
            }
            this.updateJobExecutions().then(() => {
                this.initJobListEventSource();
            });
            this.originalJobConfiguration = JSON.stringify(this.jobConfiguration);
            this.$root.$data.store.clearJobData()
        } else if (this.jobId != null) {
            this.loadJob(this.jobId).then(() => {
                this.updateJobExecutions().then(() => {
                    this.initJobListEventSource();
                });
                this.originalJobConfiguration = JSON.stringify(this.jobConfiguration)
            })
        } else {
            this.jobConfiguration = await IgorBackend.getData('/api/job/prototype');
            this.originalJobConfiguration = JSON.stringify(this.jobConfiguration);
            this.initJobListEventSource();
        }
    },
    destroyed() {
        if (this.jobListEventSource) {
            this.jobListEventSource.close()
        }
        if (this.jobExecutionEventSource) {
            this.jobExecutionEventSource.close();
        }
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

.configurator {
    flex-grow: 1;
}

.executions-top-panel {
    margin-bottom: .1em;
}

.fixed-width {
    max-width: 35em;
    min-width: 35em;
}

/* animate.css animation speed */
.animated {
    -webkit-animation-duration: var(--animate-css-duration);
    animation-duration: var(--animate-css-duration);
    -webkit-animation-fill-mode: both;
    animation-fill-mode: both;
}

</style>
