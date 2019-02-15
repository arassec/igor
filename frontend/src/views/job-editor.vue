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
                    <input-button icon="play" v-on:clicked="runJob" class="button-margin-left"
                                  :disabled="jobRunning || jobConfiguration.id == null || requestInProgress"/>
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
                <feedback-panel v-for="(jobExecution, index) in jobExecutions"
                                v-bind:key="index"
                                :feedback="formatJobExecution(jobExecution)"
                                :alert="'FAILED' === jobExecution.executionState"
                                :request-in-progress="'RUNNING' === jobExecution.executionState"/>
            </p>
        </side-menu>

        <core-content>
            <job-configurator v-show="selectedTaskIndex == -1"
                              :job-configuration="jobConfiguration"
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
      showDeleteTaskDialog: false,
      showDeleteActionDialog: false,
      initialProviderCategory: {},
      initialProviderType: {},
      initialActionCategory: {},
      initialActionType: {},
      selectedTaskIndex: -1,
      selectedActionIndex: -1,
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
      jobExecutions: [],
      jobExecutionsRefreshTimer: null
    }
  },
  computed: {
    jobRunning: function () {
      if (this.jobExecutions != null) {
        for (let i = 0; i < this.jobExecutions.length; i++) {
          if ('RUNNING' === this.jobExecutions[i].executionState) {
            return true;
          }
        }
      }
      return false
    }
  },
  methods: {
    formatJobExecution: function (jobExecution) {
      if ('RUNNING' === jobExecution.executionState) {
        return 'Running since: ' + jobExecution.started
      } else if ('WAITING' === jobExecution.executionState) {
        return 'Waiting since: ' + jobExecution.created
      } else if ('FINISHED' === jobExecution.executionState) {
        return 'Finished at: ' + jobExecution.finished
      } else if ('CANCELLED' === jobExecution.executionState) {
        return 'Cancelled at: ' + jobExecution.finished
      } else if ('FAILED' === jobExecution.executionState) {
        return 'Failed at: ' + jobExecution.finished
      } else {
        return jobExecution.executionState
      }
    },
    loadJob: function (id) {
      let component = this
      this.$http.get('/api/job/' + id).then(function (response) {
        component.jobConfiguration = response.data
        component.updateJobExecutions()
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Loading failed! (' + error + ')', true)
      })
    },
    saveConfiguration: function () {
      if (!this.validateInput()) {
        return
      }

      this.testResults = null

      let component = this

      if (this.newJob) {
        this.$http.post('/api/job', this.jobConfiguration).then(function (response) {
          component.jobConfiguration = response.data
          component.newJob = false
          component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' saved.', false)
        }).catch(function (error) {
          component.$root.$data.store.setFeedback('Saving failed! (' + error + ')', true)
        })
      } else {
        this.$http.put('/api/job', this.jobConfiguration).then(function (response) {
          component.jobConfiguration = response.data
          component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' updated.', false)

        }).catch(function (error) {
          component.$root.$data.store.setFeedback('Saving failed! (' + error + ')', true)
        })
      }
    },
    testConfiguration: function () {
      if (!this.validateInput()) {
        return
      }

      this.testResults = null
      this.selectedTestResults = null

      let component = this

      this.$http.post('/api/job/test', this.jobConfiguration).then(function (response) {
        component.testResults = response.data
        component.$root.$data.store.setFeedback('Test OK.', false)
        component.updateSelectedTestResult()
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Testing failed! (' + error + ')', true)
      })
    },
    cancelConfiguration: function () {
      this.$router.push({name: 'jobs'})
    },
    selectJob: function () {
      this.selectedTaskIndex = -1
      this.selectedActionIndex = -1
      this.selectedTestResults = null
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
    validateInput: function () {
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
        this.$root.$data.store.setFeedback('Validation failed!', true)
        return false
      }

      return true
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

      this.$http.post('/api/job/run', this.jobConfiguration).then(function (response) {
        component.jobConfiguration = response.data
        component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' started manually.', false)
        component.updateJobExecutions()
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' startup failed ('
          + error + ').', true)
      })
    },
    updateJobExecutions: function () {
      let component = this
      this.$http.get('/api/job/' + this.jobConfiguration.id + '/executions', this.jobConfiguration).then(function (response) {
        for (let i = component.jobExecutions.length; i > 0; i--) {
          component.jobExecutions.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.jobExecutions.push(item)
        })
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Background sync failed! (' + error.response.data.error + ')', true)
      })
    },
    cancelJob: function () {
      let component = this
      this.$http.post('/api/job/' + this.jobConfiguration.id + '/cancel', this.jobConfiguration).then(function () {
        component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' cancelled.', false)
        component.updateJobExecutions()
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' cancellation failed ('
          + error + ').', true)
      })
    },
    createService: function (selectionKey, parameterIndex, serviceCategory) {
      this.$root.$data.store.setJobData(this.jobConfiguration, selectionKey, parameterIndex, serviceCategory)
      this.$router.push({name: 'service-editor'})
    }
  },
  mounted () {
    let jobData = this.$root.$data.store.getJobData()
    if (jobData.jobConfiguration != null) {
      this.jobConfiguration = jobData.jobConfiguration
      if (this.jobConfiguration.id != null) {
        this.newJob = false
        this.updateJobExecutions()
        this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
      }

      let selectionKey = jobData.selectionKey
      if (selectionKey != null) {
        let taskIndex = -1
        let actionIndex = -1
        if ((typeof selectionKey === 'string' || selectionKey instanceof String) && selectionKey.includes("_")) {
          taskIndex = selectionKey.split("_")[0]
          actionIndex = selectionKey.split("_")[1]
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

      this.$root.$data.store.clearJobData()
    } else if (this.jobId != null) {
      this.newJob = false
      this.loadJob(this.jobId)
      this.jobExecutionsRefreshTimer = setInterval(() => this.updateJobExecutions(), 1000)
    }

    let component = this

    this.$http.get('/api/category/provider').then(function (response) {
      component.initialProviderCategory = Array.from(response.data)[0]
      component.$http.get('/api/type/provider/' + component.initialProviderCategory.key).then(function (response) {
        component.initialProviderType = Array.from(response.data)[0]
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Could not load provider types (' + error + ')', true)
      })
    }).catch(function (error) {
      component.$root.$data.store.setFeedback('Could not load provider categories (' + error + ')', true)
    })
    this.$http.get('/api/category/action').then(function (response) {
      component.initialActionCategory = Array.from(response.data)[0]
      component.$http.get('/api/type/action/' + component.initialActionCategory.key).then(function (response) {
        component.initialActionType = Array.from(response.data)[0]
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Could not load action types (' + error + ')', true)
      })
    }).catch(function (error) {
      component.$root.$data.store.setFeedback('Could not load action categories (' + error + ')', true)
    })
  },
  destroyed () {
    clearInterval(this.jobExecutionsRefreshTimer)
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
