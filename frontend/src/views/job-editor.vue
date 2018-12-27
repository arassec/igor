<template>
    <core-container>

        <side-menu>
            <p slot="title">Job Configuration</p>
            <button-row slot="buttons" v-on:cancel-configuration="cancelConfiguration">
                <p slot="left">
                    <input-button icon="arrow-left" v-on:clicked="cancelConfiguration"
                                  class="button-margin-right"/>
                    <input-button icon="plug" v-on:clicked="testConfiguration" class="button-margin-right"
                                  :disabled="jobRunning"/>
                    <input-button icon="save" v-on:clicked="saveConfiguration" :disabled="jobRunning"/>
                </p>
                <p slot="right">
                    <input-button icon="times" v-on:clicked="cancelJob" :disabled="!jobRunning"/>
                    <input-button icon="play" v-on:clicked="runJob" class="button-margin-left" :disabled="jobRunning"/>
                </p>
            </button-row>
            <job-tree-navigation slot="content"
                                 :job-configuration="jobConfiguration"
                                 :validation-errors="validationErrors"
                                 :selected-task-index="selectedTaskIndex"
                                 :selected-action-index="selectedActionIndex"
                                 v-on:job-is-selected="selectJob"
                                 v-on:task-is-selected="selectTask"
                                 v-on:action-is-selected="selectAction"
                                 v-on:add-task="addTask"
                                 v-on:delete-task="showDeleteTask"
                                 v-on:add-action="addAction"
                                 v-on:delete-action="showDeleteAction"
                                 v-on:move-task-up="moveTaskUp"
                                 v-on:move-task-down="moveTaskDown"
                                 v-on:move-action-up="moveActionUp"
                                 v-on:move-action-down="moveActionDown">
            </job-tree-navigation>
            <p slot="feedback">
                <feedback-panel :feedback="feedback" :alert="!feedbackOk"
                                :requestInProgress="requestInProgress"/>
                <feedback-panel v-if="jobRunning" :feedback="'Job is currently running!'" :alert="false"
                                :request-in-progress="true"/>
            </p>
        </side-menu>

        <core-content>
            <job-configurator v-show="selectedTaskIndex == -1"
                              :job-configuration="jobConfiguration"
                              ref="jobConfigurator"/>

            <task-configurator v-for="(task, taskIndex) in jobConfiguration.tasks"
                               v-show="selectedTaskIndex == taskIndex && selectedActionIndex == -1"
                               v-bind:key="taskIndex"
                               v-bind:task="task"
                               ref="taskConfigurators"/>

            <template v-for="(task, taskIndex) in jobConfiguration.tasks">
                <action-configurator v-for="(action, actionIndex) in task.actions"
                                     v-show="selectedTaskIndex == taskIndex && selectedActionIndex == actionIndex"
                                     v-bind:key="taskIndex + '_' + actionIndex"
                                     v-bind:action-key="taskIndex + '_' + actionIndex"
                                     v-bind:action="action"
                                     v-bind:action-types="actionTypes"
                                     ref="actionConfigurators"/>
            </template>

            <modal-dialog v-if="showDeleteTaskDialog" @close="showDeleteTaskDialog = false">
                <p slot="header">Delete Task?</p>
                <p slot="body">Do you really want to delete this Task?</p>
                <div slot="footer">
                    <button-row>
                        <input-button slot="left" v-on:clicked="showDeleteTaskDialog = false" icon="times"/>
                        <input-button slot="right" v-on:clicked="deleteTask()" icon="check"/>
                    </button-row>
                </div>
            </modal-dialog>

            <modal-dialog v-if="showDeleteActionDialog" @close="showDeleteActionDialog = false">
                <p slot="header">Delete Action?</p>
                <p slot="body">Do you really want to delete this Action?</p>
                <div slot="footer">
                    <button-row>
                        <input-button slot="left" v-on:clicked="showDeleteActionDialog = false" icon="times"/>
                        <input-button slot="right" v-on:clicked="deleteAction()" icon="check"/>
                    </button-row>
                </div>
            </modal-dialog>

        </core-content>

        <test-result-container v-if="testResults != null && !(selectedTaskIndex == -1 && selectedActionIndex == -1)"
                               v-on:close="testResults = null"
                               v-bind:selected-test-results="selectedTestResults"/>

    </core-container>
</template>

<script>
import CoreContainer from '../components/common/core-container'
import CoreContent from '../components/common/core-content'
import FeedbackPanel from '../components/common/feedback-panel'
import JobTreeNavigation from '../components/jobs/job-tree-navigation'
import JobConfigurator from '../components/jobs/job-configurator'
import TaskConfigurator from '../components/jobs/task-configurator'
import ActionConfigurator from '../components/jobs/action-configurator'
import ModalDialog from '../components/common/modal-dialog'
import ButtonRow from '../components/common/button-row'
import InputButton from '../components/common/input-button'
import TestResultContainer from '../components/jobs/test-result-container'
import SideMenu from '../components/common/side-menu'

export default {
  name: 'job-editor',
  components: {
    SideMenu,
    TestResultContainer,
    InputButton,
    ButtonRow,
    ModalDialog,
    ActionConfigurator,
    TaskConfigurator,
    JobConfigurator,
    JobTreeNavigation,
    FeedbackPanel,
    CoreContent,
    CoreContainer
  },
  props: ['jobId'],
  data: function () {
    return {
      newJob: true,
      feedback: '',
      feedbackOk: true,
      showDeleteTaskDialog: false,
      showDeleteActionDialog: false,
      selectedTaskIndex: -1,
      selectedActionIndex: -1,
      actionTypes: [],
      requestInProgress: false,
      testResults: null,
      selectedTestResults: null,
      jobConfiguration: {
        name: 'New Job',
        trigger: '',
        description: '',
        active: true,
        tasks: []
      },
      validationErrors: [],
      jobExecution: null,
      jobExecutionRefreshTimer: null
    }
  },
  computed: {
    jobRunning: function () {
      if (this.jobExecution != null && this.jobExecution != '') {
        return ('RUNNING' === this.jobExecution.executionState)
      }
      return false
    }
  },
  methods: {
    loadJob: function (id) {
      this.feedback = ''
      this.feedbackOk = true
      let component = this
      this.$http.get('/api/job/' + id).then(function (response) {
        component.jobConfiguration = response.data
        component.updateJobExecution()
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    saveConfiguration: function () {
      if (!this.validateInput()) {
        return
      }

      this.testResults = null

      this.feedback = 'Saving...'
      this.requestInProgress = true

      let component = this

      if (this.newJob) {
        this.$http.post('/api/job', this.jobConfiguration).then(function () {
          component.feedback = ''
          component.feedbackOk = true
          component.requestInProgress = false
          component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' saved.', false)
        }).catch(function (error) {
          component.feedback = 'Saving failed! (' + error.response.data.error + ')'
          component.feedbackOk = false
          component.requestInProgress = false
        })
      } else {
        this.$http.put('/api/job', this.jobConfiguration).then(function () {
          component.feedback = ''
          component.feedbackOk = true
          component.requestInProgress = false
          component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' updated.', false)
        }).catch(function (error) {
          component.feedback = 'Saving failed! (' + error.response.data.error + ')'
          component.feedbackOk = false
          component.requestInProgress = false
        })
      }
    },
    testConfiguration: function () {
      if (!this.validateInput()) {
        return
      }

      this.testResults = null
      this.selectedTestResults = null
      this.feedback = 'Testing...'
      this.requestInProgress = true

      let component = this

      this.$http.post('/api/job/test', this.jobConfiguration).then(function (response) {
        component.testResults = response.data
        component.feedback = 'OK'
        component.feedbackOk = true
        component.requestInProgress = false
        component.updateSelectedTestResult()
      }).catch(function (error) {
        component.feedback = 'Testing failed! (' + error.response.data.error + ')'
        component.feedbackOk = false
        component.requestInProgress = false
      })
    },
    cancelConfiguration: function () {
      this.$router.push({name: 'jobs'})
    },
    selectJob: function () {
      this.selectedTaskIndex = -1
      this.selectedActionIndex = -1
      this.selectedTestResults = null
      this.validateInput()
    },
    selectTask: function (taskIndex) {
      this.selectedTaskIndex = taskIndex
      this.selectedActionIndex = -1
      this.updateSelectedTestResult()
      this.validateInput()
    },
    selectAction: function (taskIndex, actionIndex) {
      this.selectedTaskIndex = taskIndex
      this.selectedActionIndex = actionIndex
      this.updateSelectedTestResult()
      this.validateInput()
    },
    addTask: function () {
      let task = {
        name: 'Task',
        description: '',
        provider: {
          type: '',
          parameters: {}
        },
        actions: []
      }
      this.jobConfiguration.tasks.push(task)
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
        type: this.actionTypes[0].type,
        label: this.actionTypes[0].label,
        parameters: {}
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
    },
    moveActionDown: function (taskIndex, actionIndex) {
      if (actionIndex < this.jobConfiguration.tasks[taskIndex].actions.length - 1) {
        this.arrayMove(this.jobConfiguration.tasks[taskIndex].actions, actionIndex, actionIndex + 1)
        this.selectAction(taskIndex, (actionIndex + 1))
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
    validateInput: function () {
      this.feedback = ''
      this.feedbackOk = true
      this.validationErrors = []

      let jobConfiguratorResult = this.$refs.jobConfigurator.validateInput()
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
        this.feedback = 'Validation failed'
        this.feedbackOk = false
      }

      return this.feedbackOk
    },
    updateSelectedTestResult: function () {
      if (this.testResults != null) {
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
    },
    loadActionTypes: function () {
      let component = this
      this.$http.get('/api/actiontype').then(function (response) {
        for (let i = component.actionTypes.length; i > 0; i--) {
          component.actionTypes.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.actionTypes.push(item)
        })
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    isTaskSelected: function (index) {
      return (index == this.selectedTaskIndex && this.selectedActionIndex == -1)
    },
    isActionSelected: function (taskIndex, actionIndex) {
      return taskIndex == this.selectedTaskIndex && actionIndex == this.selectedActionIndex
    },
    runJob: function () {
      if (!this.validateInput()) {
        return
      }

      let component = this

      this.$http.post('/api/job/run', this.jobConfiguration).then(function () {
        component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' started manually.', false)
        component.updateJobExecution()
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' startup failed ('
          + error + ').', true)
      })
    },
    updateJobExecution: function () {
      let component = this
      this.$http.get('/api/job/' + this.jobConfiguration.id + '/execution', this.jobConfiguration).then(function (response) {
        component.jobExecution = response.data
      }).catch(function (error) {
        component.feedback = 'Background sync failed! (' + error.response.data.error + ')'
        component.feedbackOk = false
        component.requestInProgress = false
      })
    },
    cancelJob: function () {
      let component = this
      this.$http.post('/api/job/' + this.jobConfiguration.id + '/cancel', this.jobConfiguration).then(function () {
        component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' cancelled.', false)
        component.updateJobExecution()
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' cancellation failed ('
          + error + ').', true)
      })
    }
  },
  mounted () {
    if (this.jobId != null) {
      this.newJob = false
      this.loadJob(this.jobId)
      this.jobExecutionRefreshTimer = setInterval(() => this.updateJobExecution(), 1000);
    }
    this.loadActionTypes()
  },
  destroyed () {
    clearInterval(this.jobExecutionRefreshTimer)
  }
}
</script>

<style scoped>

    .button-panel {
        margin-top: 25px;
    }

    .add-task-button {
        margin-top: 25px;
        color: var(--font-color-light);
        background-color: var(--panel-background-color);
    }

</style>
