<template>
  <core-container>

    <spacer-item/>

    <core-content>
      <core-panel>
        <h1>{{ jobConfiguration.name.length > 0 ? 'Job: ' + jobConfiguration.name : 'New Job' }}</h1>

        <table>
          <tr>
            <td><label>Name</label></td>
            <td>
              <input type="text" autocomplete="off" v-model="jobConfiguration.name"/>
            </td>
            <td>
              <validation-error v-if="nameValidationError.length > 0">
                {{nameValidationError}}
              </validation-error>
            </td>
          </tr>
          <tr>
            <td><label for="trigger-input">Trigger</label></td>
            <td>
              <input id="trigger-input" type="text" autocomplete="off"
                     v-model="jobConfiguration.trigger"/>
              <input-button v-on:clicked="showCronTrigger = true" icon="clock"/>
            </td>
            <td>
              <validation-error v-if="triggerValidationError.length > 0">
                {{triggerValidationError}}
              </validation-error>
            </td>
          </tr>
          <tr>
            <td><label for="description-input">Description</label></td>
            <td>
              <input id="description-input" type="text" autocomplete="off"
                     v-model="jobConfiguration.description"/>
            </td>
            <td/>
          </tr>
          <tr>
            <td><label>Active</label></td>
            <td>
              <font-awesome-icon :icon="jobConfiguration.active ? 'check-square' : 'square'"
                                 v-on:click="jobConfiguration.active = !jobConfiguration.active"/>
            </td>
            <td/>
          </tr>
        </table>
      </core-panel>

      <cron-picker v-show="showCronTrigger" v-on:selected="setCronTrigger" v-on:cancel="showCronTrigger = false"/>

      <task-editor v-for="(task, index) in jobConfiguration.tasks"
                   v-bind:task="task"
                   v-bind:index="index"
                   v-bind:key="index"
                   v-bind:test-results="testResults"
                   v-bind:task-index="index"
                   v-on:delete="deleteTask(index)"
                   v-on:show-task-test-results="showTaskTestResult(index)"
                   v-on:show-action-test-results="showActionTestResult"
                   ref="taskEditors"/>

      <core-panel class="button-panel">
        <button-row>
          <label-button slot="right" v-on:clicked="addTask()" icon="plus" label="Add Task"/>
        </button-row>
      </core-panel>

      <core-panel class="button-panel">
        <feedback-panel :feedback="feedback" :alert="!feedbackOk" :requestInProgress="requestInProgress"/>

        <button-row>
          <p slot="left">
            <input-button v-on:clicked="cancel()" icon="times"/>
          </p>

          <p slot="right">
            <input-button v-on:clicked="testConfiguration()" icon="plug"/>
            <input-button v-on:clicked="saveConfiguration()" icon="save"/>
          </p>
        </button-row>
      </core-panel>

    </core-content>

    <test-result-container :selected-test-results="selectedTestResults" :heading="'Test results'"
                           v-if="selectedTestResults != null" v-on:close="selectedTestResults = null"/>

    <spacer-item/>

  </core-container>
</template>

<script>
import TaskEditor from './task-editor'
import SpacerItem from '../common/spacer-item'
import CorePanel from '../common/core-panel'
import InputButton from '../common/input-button'
import CoreContainer from '../common/core-container'
import CoreContent from '../common/core-content'
import ButtonRow from '../common/button-row'
import ValidationError from '../common/validation-error'
import FeedbackPanel from '../common/feedback-panel'
import CronPicker from '../common/cron-picker'
import TestResultContainer from './test-result-container'
import LabelButton from '../common/label-button'

export default {
  name: 'job-editor',
  components: {
    LabelButton,
    TestResultContainer,
    CronPicker,
    FeedbackPanel,
    ValidationError,
    ButtonRow,
    CoreContent,
    CoreContainer,
    CorePanel,
    InputButton,
    SpacerItem,
    TaskEditor
  },
  props: ['jobId'],
  data: function () {
    return {
      newJob: true,
      feedback: '',
      feedbackOk: true,
      requestInProgress: false,
      nameValidationError: '',
      triggerValidationError: '',
      showCronTrigger: false,
      testResults: null,
      selectedTestResults: null,
      jobConfiguration: {
        name: '',
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
    addTask: function () {
      let task = {
        name: '',
        description: '',
        provider: {
          type: '',
          parameters: {}
        },
        actions: []
      }
      this.jobConfiguration.tasks.push(task)
    },
    deleteTask: function (index) {
      this.$delete(this.jobConfiguration.tasks, index)
    },
    validateInput: function () {
      this.feedback = ''
      this.feedbackOk = true
      this.nameValidationError = ''
      this.triggerValidationError = ''

      let nameValidationResult = true
      if (this.jobConfiguration.name == null || this.jobConfiguration.name === '') {
        this.nameValidationError = 'Name must be set'
        nameValidationResult = false
      }

      let triggerValidationResult = true
      if (this.jobConfiguration.trigger == null || this.jobConfiguration.trigger === '') {
        this.triggerValidationError = 'Trigger must be set'
        triggerValidationResult = false
      }

      let taskEditorsResult = true
      for (let i in this.$refs.taskEditors) {
        taskEditorsResult = (this.$refs.taskEditors[i].validateInput() && taskEditorsResult)
      }

      return (nameValidationResult && triggerValidationResult && taskEditorsResult)
    },
    cancel: function () {
      this.$router.back()
    },
    setCronTrigger: function (value) {
      this.jobConfiguration.trigger = value
      this.showCronTrigger = false
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
    }
  },
  mounted () {
    if (this.jobId != null) {
      this.newJob = false
      this.loadJob(this.jobId)
    }
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
