<template>
    <core-container>

        <side-menu v-if="jobConfiguration">
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
                                    v-on:cancel-configuration="cancelConfiguration"
                                    v-on:test-configuration="testConfiguration"
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
                    <div v-if="showExecutions">
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
                                v-on:feedback-clicked="openExecutionDetailsDialog(jobExecution)">
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
                                    <input-button slot="right" icon="times"
                                                  v-on:clicked="openCancelJobDialog(jobExecution)"
                                                  v-if="jobExecution.state === 'WAITING' || jobExecution.state === 'RUNNING' || jobExecution.state === 'ACTIVE'"/>
                                    <input-button slot="right" icon="check"
                                                  v-on:clicked="openMarkJobExecutionResolvedDialog(jobExecution)"
                                                  v-if="jobExecution.state === 'FAILED'"/>
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
                v-on:open-documentation="openDocumentation"
                v-on:switch-documentation="switchDocumentation"
                v-on:close-documentation="closeDocumentation"
                ref="jobConfigurator"/>

            <action-configurator v-for="action in jobConfiguration.actions"
                                 v-show="selectedActionId === action.id"
                                 :key="action.id"
                                 :action="action"
                                 :validation-errors="validationErrors"
                                 v-on:create-connector="createConnector"
                                 v-on:open-documentation="openDocumentation"
                                 v-on:switch-documentation="switchDocumentation"
                                 v-on:close-documentation="closeDocumentation"
                                 ref="actionConfigurators"/>
        </core-content>

        <transition v-on:after-leave="blendInDocumentation"
                    name="animate-css-transition"
                    enter-active-class="animated slideInRight"
                    leave-active-class="animated slideOutRight">
            <test-result-container v-show="showTestResults && testResults != null"
                                   v-on:close="closeTestResults"
                                   v-bind:selected-test-results="selectedTestResults"/>
        </transition>

        <transition v-on:after-leave="blendInTestResults"
                    name="animate-css-transition"
                    enter-active-class="animated slideInRight"
                    leave-active-class="animated slideOutRight">
            <documentation-container :documentation="documentation" v-show="showDocumentation"
                                     v-on:close="closeDocumentation"/>
        </transition>

        <job-execution-details v-show="showExecutionDetailsDialog"
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
                <input-button slot="right" v-on:clicked="runJob()" icon="check"/>
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
import TestResultContainer from '../components/jobs/test-result-container'
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
        TestResultContainer,
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
            showTestResults: false,
            shouldShowTestResults: false,
            showDocumentation: false,
            shouldShowDocumentation: false,
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
                for (let i = 0; i < this.jobExecutionsPage.items.length; i++) {
                    if ('RUNNING' === this.jobExecutionsPage.items[i].state
                        || 'WAITING' === this.jobExecutionsPage.items[i].state
                        || 'ACTIVE' === this.jobExecutionsPage.items[i].state) {
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
        blendInTestResults: function () {
            if (this.shouldShowTestResults) {
                this.showTestResults = true;
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
        testConfiguration: async function () {
            this.showDocumentation = false;
            this.testResults = null;
            this.selectedTestResults = null;
            await IgorBackend.postData('/api/job/simulate', this.jobConfiguration, 'Simulating job',
                'Simulation OK.', 'Simulation Failed!').then((result) => {
                if (result.status === 400) {
                    if (result.data === undefined || result.data === "") {
                        this.validationErrors = {};
                    } else {
                        this.validationErrors = result.data;
                    }
                } else {
                    this.validationErrors = {};
                    this.testResults = result.data;
                }
                this.showTestResults = true;
            });
            this.updateSelectedTestResult()
        },
        cancelConfiguration: function () {
            this.$router.push({name: 'job-overview'})
        },
        selectJob: function () {
            this.selectedActionId = null;
            this.showDocumentation = false;
            this.updateSelectedTestResult()
        },
        selectAction: function (actionId) {
            this.selectedActionId = actionId;
            this.showDocumentation = false;
            this.updateSelectedTestResult()
        },
        addAction: async function () {
            let action = await IgorBackend.getData('/api/job/action/prototype');
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
            this.testResults = null;
        },
        updateSelectedTestResult: function () {
            if (this.testResults != null) {
                let actionId = this.selectedActionId;

                // The job has been selected, use the trigger's test data:
                if (actionId === null) {
                    if (this.testResults[this.jobConfiguration.id]) {
                        this.selectedTestResults = this.testResults[this.jobConfiguration.id]
                    } else {
                        this.selectedTestResults = null;
                    }
                    return
                }

                // An Action has been selected:
                if (actionId != null && this.testResults[actionId] != null) {
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
                    this.showConfiguration = false;
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
            if (this.showTestResults) {
                this.showTestResults = false;
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
            if (this.testResults) {
                this.shouldShowTestResults = true;
                this.shouldShowDocumentation = false;
                this.showDocumentation = false;
            } else {
                this.showDocumentation = false;
                this.shouldShowDocumentation = false;
            }
        },
        closeTestResults: function () {
            if (this.testResults) {
                this.testResults = null;
                this.shouldShowTestResults = false;
                this.showTestResults = false;
            }
        },
        initJobListEventSource: function () {
            if (this.jobListEventSource) {
                this.jobListEventSource.close();
            }
            this.jobListEventSource = new EventSource('api/job/stream');
            let component = this;
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
                setTimeout(this.initJobListEventSource, 5000);
            }
        },
        initJobExecutionEventSource: function () {
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
