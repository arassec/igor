<template>
    <core-container>

        <job-tree-navigation :job-configuration="jobConfiguration"
                             :job-selected="jobSelected"
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
                             v-on:move-action-down="moveActionDown"
                             v-on:test-job="testConfiguration"
                             v-on:save-job="saveConfiguration">
            <feedback-panel slot="feedback" :feedback="feedback" :alert="!feedbackOk"
                            :requestInProgress="requestInProgress"/>
        </job-tree-navigation>

        <core-content>
            <job-configurator v-if="jobSelected" :job-configuration="jobConfiguration"/>

            <task-configurator v-if="selectedTask != null" :task="selectedTask"/>

            <action-configurator v-if="selectedAction != null" :action="selectedAction"
                            :action-types="actionTypes"/>

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

export default {
  name: 'job-editor',
  components: {
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
      jobSelected: true,
      selectedTask: null,
      selectedAction: null,
      showDeleteTaskDialog: false,
      showDeleteActionDialog: false,
      selectedTaskIndex: -1,
      selectedActionIndex: -1,
      actionTypes: [],
      requestInProgress: false,
      testResults: null,
      selectedTestResults: null,
      jobConfiguration: {
        name: 'Job',
        trigger: '',
        description: '',
        active: true,
        tasks: []
      }
    }
  },
  methods: {
    loadJob: function (id) {
      this.feedback = ''
      this.feedbackOk = true
      let component = this
      this.$http.get('/api/job/' + id).then(function (response) {
        component.jobConfiguration = response.data
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    saveConfiguration: function () {
      if (!this.validateInput()) {
        return
      }

      this.feedback = 'Saving...'
      this.requestInProgress = true

      let component = this

      if (this.newJob) {
        this.$http.post('/api/job', this.jobConfiguration).then(function () {
          component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' saved.', false)
          component.$router.push({name: 'jobs'})
        }).catch(function (error) {
          component.feedback = 'Saving failed! (' + error.response.data.error + ')'
          component.feedbackOk = false
          component.requestInProgress = false
        })
      } else {
        this.$http.put('/api/job', this.jobConfiguration).then(function () {
          component.$root.$data.store.setFeedback('Job \'' + component.jobConfiguration.name + '\' updated.', false)
          component.$router.push({name: 'jobs'})
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
      }).catch(function (error) {
        component.feedback = 'Testing failed! (' + error.response.data.error + ')'
        component.feedbackOk = false
        component.requestInProgress = false
      })
    },
    selectJob: function () {
      this.jobSelected = true
      this.selectedTask = null
      this.selectedAction = null
      this.selectedTaskIndex = -1
      this.selectedActionIndex = -1
    },
    selectTask: function (taskIndex) {
      this.jobSelected = false
      this.selectedTask = this.jobConfiguration.tasks[taskIndex]
      this.selectedAction = null
      this.selectedTaskIndex = taskIndex
      this.selectedActionIndex = -1
    },
    selectAction: function (taskIndex, actionIndex) {
      this.jobSelected = false
      this.selectedTask = null
      this.selectedAction = this.jobConfiguration.tasks[taskIndex].actions[actionIndex]
      this.selectedTaskIndex = taskIndex
      this.selectedActionIndex = actionIndex
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
    },
    showDeleteTask: function (taskIndex) {
      this.selectedTaskIndex = taskIndex
      this.showDeleteTaskDialog = true
    },
    deleteTask: function () {
      this.$delete(this.jobConfiguration.tasks, this.selectedTaskIndex)
      this.showDeleteTaskDialog = false
      this.jobSelected = true
      this.selectedTaskIndex = -1
      this.selectedActionIndex = -1
      this.selectedTask = null
      this.selectedAction = null
    },
    addAction: function (taskIndex) {
      let action = {
        type: this.actionTypes[0].type,
        label: this.actionTypes[0].label,
        parameters: {}
      }
      this.jobConfiguration.tasks[taskIndex].actions.push(action)
    },
    showDeleteAction: function (taskIndex, actionIndex) {
      this.selectedTaskIndex = taskIndex
      this.selectedActionIndex = actionIndex
      this.showDeleteActionDialog = true
    },
    deleteAction: function () {
      this.jobConfiguration.tasks[this.selectedTaskIndex].actions.splice(this.selectedActionIndex, 1)
      this.showDeleteActionDialog = false
      this.jobSelected = true
      this.selectedTaskIndex = -1
      this.selectedActionIndex = -1
      this.selectedTask = null
      this.selectedAction = null
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



      let nameValidationResult = true
      if (this.jobConfiguration.name == null || this.jobConfiguration.name === '') {
        nameValidationResult = false
      }

      let triggerValidationResult = true
      if (this.jobConfiguration.trigger == null || this.jobConfiguration.trigger === '') {
        triggerValidationResult = false
      }

      let result = (nameValidationResult && triggerValidationResult)
      if (!result) {
        this.feedback = 'Validation failed!'
        this.feedbackOk = false
      }

      return result
    },
    validateTaskInput: function () {

    },
    validateActionInput: function () {

    },
    showTaskTestResult: function (index) {
      if (this.testResults != null && this.testResults.taskResults != null) {
        if (this.testResults.taskResults[index] != null) {
          this.selectedTestResults = this.testResults.taskResults[index].providerResults
        }
      }
    },
    showActionTestResult: function (taskIndex, actionIndex) {
      if (this.testResults != null && this.testResults.taskResults != null) {
        let taskResults = this.testResults.taskResults
        if (taskResults[taskIndex] != null && taskResults[taskIndex].actionResults != null) {
          let actionResults = taskResults[taskIndex].actionResults
          if (actionResults[actionIndex] != null) {
            this.selectedTestResults = this.testResults.taskResults[taskIndex].actionResults[actionIndex].results
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
  },
  mounted () {
    if (this.jobId != null) {
      this.newJob = false
      this.loadJob(this.jobId)
    }
    this.loadActionTypes()
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
