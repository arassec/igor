<template>
    <core-container>

        <side-menu class="side-menu-large" v-if="jobConfiguration">
            <p slot="title">Job Configuration</p>
            <layout-row slot="header" v-on:cancel-configuration="cancelConfiguration">
                <p slot="left">
                    <input-button icon="arrow-left" v-on:clicked="cancelConfiguration"
                                  class="button-margin-right"/>
                    <input-button icon="plug" v-on:clicked="testConfiguration" class="button-margin-right"/>
                    <input-button icon="save" v-on:clicked="saveConfiguration"/>
                </p>
                <p slot="right">
                    <input-button icon="play" v-on:clicked="showRunDialog = true" class="button-margin-left"
                                  :disabled="jobRunningOrWaiting || jobConfiguration.id == null || !jobConfiguration.active"/>
                </p>
            </layout-row>
            <job-tree-navigation slot="content"
                                 :job-configuration="jobConfiguration"
                                 :validation-errors="validationErrors"
                                 :selected-task-index="selectedTaskIndex"
                                 :selected-action-index="selectedActionIndex"
                                 v-on:job-is-selected="selectJob"
                                 v-on:task-is-selected="selectTask"
                                 v-on:action-is-selected="selectAction"
                                 v-on:add-task="addTask"
                                 v-on:duplicate-task="duplicateTask"
                                 v-on:delete-task="showDeleteTask"
                                 v-on:add-action="addAction"
                                 v-on:delete-action="showDeleteAction"
                                 v-on:move-task-up="moveTaskUp"
                                 v-on:move-task-down="moveTaskDown"
                                 v-on:move-action-up="moveActionUp"
                                 v-on:move-action-down="moveActionDown">
            </job-tree-navigation>
            <div slot="footer" v-if="jobExecutionsPage && jobExecutionsPage.items">
                <feedback-box
                        v-for="(jobExecution, index) in jobExecutionsPage.items"
                        :key="index" :alert="jobExecution.state === 'FAILED'" :clickable="true"
                        v-on:feedback-clicked="openExecutionDetailsDialog(jobExecution)">
                    <div slot="left">{{formatJobExecution(jobExecution)}}</div>
                    <div slot="right">
                        <input-button slot="right" icon="times" v-on:clicked="openCancelJobDialog(jobExecution)"
                                      v-if="jobExecution.state === 'WAITING' || jobExecution.state === 'RUNNING'"/>
                        <input-button slot="right" icon="check" v-on:clicked="openMarkJobExecutionResolvedDialog(jobExecution)"
                                      v-if="jobExecution.state == 'FAILED'"/>
                    </div>
                </feedback-box>
                <list-pager :page="jobExecutionsPage" v-if="jobExecutionsPage.totalPages > 1"
                            v-on:first="manualUpdateJobExecutions(0)"
                            v-on:previous="manualUpdateJobExecutions(jobExecutionsPage.number -1)"
                            v-on:next="manualUpdateJobExecutions(jobExecutionsPage.number + 1)"
                            v-on:last="manualUpdateJobExecutions(jobExecutionsPage.totalPages - 1)"/>
            </div>
        </side-menu>

        <core-content v-if="jobConfiguration">
            <job-configurator
                    v-show="selectedTaskIndex == -1"
                    :job-configuration="jobConfiguration"
                    v-on:update-original-job-configuration="updateOriginalJobConfiguration()"
                    ref="jobConfigurator"/>

            <task-configurator v-for="(task, taskIndex) in jobConfiguration.tasks"
                               v-show="selectedTaskIndex == taskIndex && selectedActionIndex == -1"
                               v-bind:key="taskIndex"
                               v-bind:task-key="taskIndex"
                               v-bind:task="task"
                               v-on:create-service="createService"
                               ref="taskConfigurators"/>

            <template v-for="(task, taskIndex) in jobConfiguration.tasks">
                <action-configurator v-for="(action, actionIndex) in task.actions"
                                     v-show="selectedTaskIndex == taskIndex && selectedActionIndex == actionIndex"
                                     v-bind:key="taskIndex + '_' + actionIndex"
                                     v-bind:action-key="taskIndex + '_' + actionIndex"
                                     v-bind:action="action"
                                     v-on:create-service="createService"
                                     ref="actionConfigurators"/>
            </template>

            <job-execution-details v-if="showExecutionDetailsDialog"
                                   v-bind:job-execution="selectedJobExecution"
                                   v-on:close="closeExecutionDetailsDialog()"/>

        </core-content>

        <test-result-container v-if="testResults != null"
                               v-on:close="testResults = null"
                               v-bind:error-cause="testResults.errorCause"
                               v-bind:selected-test-results="selectedTestResults"/>

        <modal-dialog v-if="showDeleteTaskDialog" @close="showDeleteTaskDialog = false">
            <h1 slot="header">Delete Task?</h1>
            <p slot="body">Do you really want to delete this Task?</p>
            <div slot="footer">
                <layout-row>
                    <input-button slot="left" v-on:clicked="showDeleteTaskDialog = false" icon="times"/>
                    <input-button slot="right" v-on:clicked="deleteTask()" icon="check"/>
                </layout-row>
            </div>
        </modal-dialog>

        <modal-dialog v-if="showDeleteActionDialog" @close="showDeleteActionDialog = false">
            <h1 slot="header">Delete Action?</h1>
            <p slot="body">Do you really want to delete this Action?</p>
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
            <p slot="body">
                Manually start job now?
            </p>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showRunDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="runJob()" icon="check"/>
            </layout-row>
        </modal-dialog>

        <modal-dialog v-if="showCancelJobDialog"
                      @close="showCancelJobDialog = false"
                      v-on:cancel="showCancelJobDialog = false">
            <h1 slot="header">Cancel job execution</h1>
            <p slot="body">
                Are you sure you want to cancel this execution?
            </p>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showCancelJobDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="cancelJobExecution()" icon="check"/>
            </layout-row>
        </modal-dialog>

        <modal-dialog v-if="showUnsavedValuesExistDialog" @close="showUnsavedValuesExistDialog = false">
            <h1 slot="header">Unsaved configuration</h1>
            <p slot="body">There are unsaved configuration changes.<br/><br/>Do you really want to leave?</p>
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
                    Mark <b>all {{numFailedExecutionsForSelectedJob}}</b> executions of this job as resolved:
                    <font-awesome-icon :icon="resolveAllFailedExecutionsOfJob ? 'check-square' : 'square'"
                                       v-on:click="resolveAllFailedExecutionsOfJob = !resolveAllFailedExecutionsOfJob"/>
                </div>
            </div>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showMarkJobExecutionResolvedDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="markJobExecutionResolved()" icon="check"/>
            </layout-row>
        </modal-dialog>

        <background-icon right="true" icon-one="toolbox"/>

    </core-container>
</template>
<script>
  import CoreContainer from '../components/common/core-container'
  import CoreContent from '../components/common/core-content'
  import JobTreeNavigation from '../components/jobs/job-tree-navigation'
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
  import FormatUtils from '../utils/format-utils.js'
  import BackgroundIcon from "../components/common/background-icon";
  import IgorBackend from '../utils/igor-backend.js'
  import ListPager from "../components/common/list-pager";

  export default {
    name: 'job-editor',
    components: {
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
      JobTreeNavigation,
      CoreContent,
      CoreContainer
    },
    props: ['jobId'],
    data: function () {
      return {
        newJob: true,
        showDeleteTaskDialog: false,
        showDeleteActionDialog: false,
        showCancelJobDialog: false,
        showExecutionDetailsDialog: false,
        showRunDialog: false,
        initialProviderCategory: {},
        initialProviderType: {},
        initialActionCategory: {},
        initialActionType: {},
        selectedTaskIndex: -1,
        selectedActionIndex: -1,
        testResults: null,
        selectedTestResults: null,
        originalJobConfiguration: null,
        jobConfiguration: null,
        validationErrors: [],
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
        nextRoute: null,
        resolveAllFailedExecutionsOfJob: false,
        numFailedExecutionsForSelectedJob: 0
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
        return FormatUtils.formatInstant(jobExecution.created)
            + ' ' + jobExecution.duration + '(' + jobExecution.state.toLowerCase() + ')'
      },
      createJob: function () {
        this.jobConfiguration = {
          name: 'New Job',
          trigger: {
            category: null,
            type: null,
            parameters: []
          },
          description: '',
          executionHistoryLimit: 5,
          active: true,
          tasks: []
        }
      },
      loadJob: async function (id) {
        this.jobConfiguration = await IgorBackend.getData('/api/job/' + id)
      },
      saveConfiguration: async function () {
        if (!(await this.validateInput())) {
          return
        }

        this.testResults = null
        if (this.newJob) {
          IgorBackend.postData('/api/job', this.jobConfiguration, 'Saving job',
              'Job \'' + FormatUtils.formatNameForSnackbar(this.jobConfiguration.name) + '\' saved.',
              'Saving failed!').then((result) => {
            if (result === 'NAME_ALREADY_EXISTS_ERROR') {
              this.validationErrors.push('-1_-1')
              this.$refs.jobConfigurator.setNameValidationError('A job with this name already exists!')
            } else {
              this.jobConfiguration = result
              this.originalJobConfiguration = JSON.stringify(this.jobConfiguration)
              this.newJob = false
              this.$root.$data.store.setFeedback('Job \'' + FormatUtils.formatNameForSnackbar(this.jobConfiguration.name) + '\' saved.', false)
              this.$router.push({name: 'job-editor', params: {jobId: this.jobConfiguration.id}})
            }
          })
        } else {
          IgorBackend.putData('/api/job', this.jobConfiguration, 'Saving job',
              'Job \'' + FormatUtils.formatNameForSnackbar(this.jobConfiguration.name) + '\' updated.',
              'Saving failed!').then(() => {
            this.originalJobConfiguration = JSON.stringify(this.jobConfiguration)
          })
        }
      },
      testConfiguration: async function () {
        if (!(await this.validateInput())) {
          return
        }

        this.testResults = null
        this.selectedTestResults = null

        let component = this

        this.$root.$data.store.setWip('Testing job')

        this.$http.post('/api/job/test', this.jobConfiguration).then(function (response) {
          component.testResults = response.data
          if (component.testResults && component.testResults.errorCause) {
            component.$root.$data.store.setFeedback('Test Failed!', true)
          } else {
            component.$root.$data.store.setFeedback('Test OK.', false)
          }
          component.$root.$data.store.clearWip()
          component.updateSelectedTestResult()
        }).catch(function (error) {
          component.$root.$data.store.setFeedback('Testing failed! (' + error + ')', true)
          component.$root.$data.store.clearWip()
        })
      },
      cancelConfiguration: function () {
        this.$router.push({name: 'app-status'})
      },
      selectJob: function () {
        this.selectedTaskIndex = -1
        this.selectedActionIndex = -1
        this.updateSelectedTestResult()
      },
      selectTask: function (taskIndex) {
        this.selectedTaskIndex = taskIndex
        this.selectedActionIndex = -1
        this.updateSelectedTestResult()
      },
      selectAction: function (taskIndex, actionIndex) {
        this.selectedTaskIndex = taskIndex
        this.selectedActionIndex = actionIndex
        this.updateSelectedTestResult()
      },
      addTask: function () {
        let task = {
          name: 'Task',
          description: '',
          active: true,
          provider: {
            category: this.initialProviderCategory,
            type: this.initialProviderType,
            parameters: []
          },
          actions: []
        }
        this.jobConfiguration.tasks.push(task)
        this.selectTask(this.jobConfiguration.tasks.length - 1)
      },
      duplicateTask: function (taskIndex) {
        let copiedTask = JSON.parse(JSON.stringify(this.jobConfiguration.tasks[taskIndex]))
        delete copiedTask.id
        this.jobConfiguration.tasks.push(copiedTask)
        this.selectTask(this.jobConfiguration.tasks.length - 1)
      },
      showDeleteTask: function (taskIndex) {
        this.selectedTaskIndex = taskIndex
        this.showDeleteTaskDialog = true
      },
      deleteTask: function () {
        this.validationErrors = []
        this.$delete(this.jobConfiguration.tasks, this.selectedTaskIndex)
        this.showDeleteTaskDialog = false
        this.selectedTaskIndex = -1
        this.selectedActionIndex = -1
      },
      addAction: function (taskIndex) {
        let action = {
          category: this.initialActionCategory,
          type: this.initialActionType,
          parameters: []
        }
        this.jobConfiguration.tasks[taskIndex].actions.push(action)
        this.selectAction(taskIndex, this.jobConfiguration.tasks[taskIndex].actions.length - 1)
      },
      showDeleteAction: function (taskIndex, actionIndex) {
        this.selectedTaskIndex = taskIndex
        this.selectedActionIndex = actionIndex
        this.showDeleteActionDialog = true
      },
      deleteAction: function () {
        this.validationErrors = []
        this.jobConfiguration.tasks[this.selectedTaskIndex].actions.splice(this.selectedActionIndex, 1)
        this.showDeleteActionDialog = false
        this.selectedTaskIndex = -1
        this.selectedActionIndex = -1
      },
      moveTaskUp: function (taskIndex) {
        if (taskIndex === 0) {
          return
        }
        this.arrayMove(this.jobConfiguration.tasks, taskIndex, taskIndex - 1)
        this.selectTask((taskIndex - 1))
      },
      moveTaskDown: function (taskIndex) {
        if (taskIndex < this.jobConfiguration.tasks.length - 1) {
          this.arrayMove(this.jobConfiguration.tasks, taskIndex, taskIndex + 1)
          this.selectTask((taskIndex + 1))
        }
      },
      moveActionUp: function (taskIndex, actionIndex) {
        if (actionIndex === 0) {
          return
        }
        this.arrayMove(this.jobConfiguration.tasks[taskIndex].actions, actionIndex, actionIndex - 1)
        this.selectAction(taskIndex, (actionIndex - 1))
        this.validationErrors = []
      },
      moveActionDown: function (taskIndex, actionIndex) {
        if (actionIndex < this.jobConfiguration.tasks[taskIndex].actions.length - 1) {
          this.arrayMove(this.jobConfiguration.tasks[taskIndex].actions, actionIndex, actionIndex + 1)
          this.selectAction(taskIndex, (actionIndex + 1))
          this.validationErrors = []
        }
      },
      arrayMove: function (array, oldIndex, newIndex) {
        if (newIndex >= array.length) {
          let k = newIndex - array.length + 1
          while (k--) {
            array.push(undefined)
          }
        }
        array.splice(newIndex, 0, array.splice(oldIndex, 1)[0])
      },
      validateInput: async function () {
        this.validationErrors = []

        let jobConfiguratorResult = await this.$refs.jobConfigurator.validateInput()
        if (!jobConfiguratorResult) {
          this.validationErrors.push('-1_-1')
        }

        let taskConfiguratorsResult = true
        for (let i in this.$refs.taskConfigurators) {
          let result = this.$refs.taskConfigurators[i].validateInput()
          if (!result) {
            this.validationErrors.push(i + '_-1')
          }
          taskConfiguratorsResult = (result && taskConfiguratorsResult)
        }

        let actionConfiguratorsResult = true
        for (let i in this.$refs.actionConfigurators) {
          let result = this.$refs.actionConfigurators[i].validateInput()
          if (!result) {
            this.validationErrors.push(this.$refs.actionConfigurators[i].actionKey)
          }
          actionConfiguratorsResult = (result && actionConfiguratorsResult)
        }

        if (!(jobConfiguratorResult && taskConfiguratorsResult && actionConfiguratorsResult)) {
          this.$root.$data.store.setFeedback('Validation failed!', true)
          return false
        }

        return true
      },
      updateSelectedTestResult: function () {
        if (this.testResults != null) {
          if (this.testResults.errorCause) {
            this.selectedTestResults = this.testResults
          } else if (this.selectedTaskIndex == -1 && this.selectedActionIndex == -1) {
            this.selectedTestResults = ''
          } else {
            let taskIndex = this.selectedTaskIndex
            let actionIndex = this.selectedActionIndex
            if (taskIndex != -1 && actionIndex != -1) {
              let taskResults = this.testResults.taskResults
              if (taskResults[taskIndex] != null && taskResults[taskIndex].actionResults != null) {
                let actionResults = taskResults[taskIndex].actionResults
                if (actionResults[actionIndex] != null) {
                  this.selectedTestResults = this.testResults.taskResults[taskIndex].actionResults[actionIndex].results
                }
              }
            } else if (taskIndex != -1) {
              if (this.testResults.taskResults[taskIndex] != null) {
                this.selectedTestResults = this.testResults.taskResults[taskIndex].providerResults
              }
            }
          }
        }
      },
      isTaskSelected: function (index) {
        return (index == this.selectedTaskIndex && this.selectedActionIndex == -1)
      },
      isActionSelected: function (taskIndex, actionIndex) {
        return taskIndex == this.selectedTaskIndex && actionIndex == this.selectedActionIndex
      },
      runJob: async function () {
        this.showRunDialog = false
        if (!this.validateInput()) {
          return
        }
        this.jobConfiguration = await IgorBackend.postData('/api/job/run', this.jobConfiguration, 'Starting job', 'Job \'' +
            FormatUtils.formatNameForSnackbar(this.jobConfiguration.name) + '\' started manually.', 'Job \'' +
            FormatUtils.formatNameForSnackbar(this.jobConfiguration.name) + '\' startup failed!')
      },
      updateJobExecutions: async function () {
        if (this.jobConfiguration.id) {
          this.jobExecutionsPage = await IgorBackend.getData('/api/execution/job/' + this.jobConfiguration.id + '?pageNumber=' +
              this.jobExecutionsPage.number + '&pageSize=' + this.jobExecutionsPage.size)
        }
      },
      manualUpdateJobExecutions: async function (page) {
        if (this.jobConfiguration.id) {
          clearInterval(this.jobExecutionsRefreshTimer)
          this.jobExecutionsPage = await IgorBackend.getData('/api/execution/job/' + this.jobConfiguration.id + '?pageNumber=' +
              page + '&pageSize=' + this.jobExecutionsPage.size)
          this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
        }
      },
      openExecutionDetailsDialog: async function (selectedJobExecutionListEntry) {
        this.selectedJobExecutionId = selectedJobExecutionListEntry.id
        this.selectedJobExecution = await IgorBackend.getData('/api/execution/details/' + this.selectedJobExecutionId)
        this.showExecutionDetailsDialog = true
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
        this.selectedJobExecutionListEntry = selectedJobExecutionListEntry
        this.showCancelJobDialog = true
      },
      cancelJobExecution: function () {
        clearInterval(this.jobExecutionsRefreshTimer)
        this.showCancelJobDialog = false
        IgorBackend.postData('/api/execution/' + this.selectedJobExecutionListEntry.id + '/cancel', null,
            "Cancelling job", "Job cancelled.", "Job could not be cancelled!").then(() => {
          this.updateJobExecutions().then(() => {
            this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
          })
        })
      },
      createService: function (selectionKey, parameterIndex, serviceCategory) {
        this.$root.$data.store.setJobData(this.jobConfiguration, selectionKey, parameterIndex, serviceCategory)
        this.$router.push({name: 'service-editor'})
      },
      updateOriginalJobConfiguration: function () {
        this.originalJobConfiguration = JSON.stringify(this.jobConfiguration)
      },
      openMarkJobExecutionResolvedDialog: async function (selectedJobExecutionListEntry) {
        this.selectedJobExecutionListEntry = selectedJobExecutionListEntry
        this.$root.$data.store.setWip('Loading job execution details...')
        this.numFailedExecutionsForSelectedJob = await IgorBackend.getData('/api/execution/job/' +
            this.selectedJobExecutionListEntry.jobId + '/FAILED/count')
        this.$root.$data.store.clearWip()
        this.showMarkJobExecutionResolvedDialog = true
      },
      markJobExecutionResolved: async function () {
        clearTimeout(this.jobExecutionsRefreshTimer)
        await IgorBackend.putData('/api/execution/' + this.selectedJobExecutionListEntry.id + '/' +
            this.selectedJobExecutionListEntry.jobId + '/FAILED/RESOLVED?updateAllOfJob=' + this.resolveAllFailedExecutionsOfJob, null,
            'Updating executions', 'Executions updated', 'Executions could not be updated!')
        await this.manualUpdateJobExecutions(0)
        this.resolveAllFailedExecutionsOfJob = false
        this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
        this.showMarkJobExecutionResolvedDialog = false
      }
    },
    mounted() {
      let jobData = this.$root.$data.store.getJobData()
      // Returning from a service configuration within a job configuration
      if (jobData.jobConfiguration != null) {
        this.jobConfiguration = jobData.jobConfiguration
        if (this.jobConfiguration.id != null) {
          this.newJob = false
        }

        let selectionKey = jobData.selectionKey
        if (selectionKey != null) {
          let taskIndex = -1
          let actionIndex = -1
          if ((typeof selectionKey === 'string' || selectionKey instanceof String) && selectionKey.includes('_')) {
            taskIndex = selectionKey.split('_')[0]
            actionIndex = selectionKey.split('_')[1]
          } else {
            taskIndex = selectionKey
          }

          if (taskIndex > -1 && actionIndex > -1) {
            if (jobData.serviceParameter != null && jobData.parameterIndex != null) {
              let parameter = this.jobConfiguration.tasks[taskIndex].actions[actionIndex].parameters[jobData.parameterIndex]
              parameter.serviceName = jobData.serviceParameter.name
              parameter.value = jobData.serviceParameter.id
            }
            this.selectAction(taskIndex, actionIndex)
          } else if (taskIndex > -1) {
            if (jobData.serviceParameter != null && jobData.parameterIndex != null) {
              let parameter = this.jobConfiguration.tasks[taskIndex].provider.parameters[jobData.parameterIndex]
              parameter.serviceName = jobData.serviceParameter.name
              parameter.value = jobData.serviceParameter.id
            }
            this.selectTask(taskIndex)
          }
        }
        this.updateJobExecutions().then(() => {
          this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
        })
        this.originalJobConfiguration = JSON.stringify(this.jobConfiguration)
        this.$root.$data.store.clearJobData()
      } else if (this.jobId != null) {
        this.newJob = false
        this.loadJob(this.jobId).then(() => {
          this.updateJobExecutions().then(() => {
            this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
          })
          this.originalJobConfiguration = JSON.stringify(this.jobConfiguration)
        })
      } else {
        // The job-configurator loads trigger data and modifies the initial jobConfiguration. So the 'originalJobConfiguration' property is set there...
        this.createJob()
        this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
      }

      let component = this
      IgorBackend.getData('/api/category/provider').then((categoryResult) => {
        this.initialProviderCategory = Array.from(categoryResult)[0]
        IgorBackend.getData('/api/type/provider/' + component.initialProviderCategory.key).then((typeResult) => {
          component.initialProviderType = Array.from(typeResult)[0]
        })
      })
      IgorBackend.getData('/api/category/action').then((categoryResult) => {
        this.initialActionCategory = Array.from(categoryResult)[0]
        IgorBackend.getData('/api/type/action/' + component.initialActionCategory.key).then((typeResult) => {
          component.initialActionType = Array.from(typeResult)[0]
        })
      })
    },
    destroyed() {
      clearInterval(this.jobExecutionsRefreshTimer)
      clearInterval(this.jobExecutionDetailsRefreshTimer)
    },
    beforeRouteLeave(to, from, next) {
      // We leave the job editor to create a new service. No unsaved-values-check required!
      let jobData = this.$root.$data.store.getJobData()
      if (jobData.jobConfiguration) {
        next()
      } else {
        if (this.originalJobConfiguration) {
          let newJobConfiguration = JSON.stringify(this.jobConfiguration)
          if (!(this.originalJobConfiguration === newJobConfiguration)) {
            this.nextRoute = next
            this.showUnsavedValuesExistDialog = true
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
        margin-bottom: 20px;
    }

</style>
