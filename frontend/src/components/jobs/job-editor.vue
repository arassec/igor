<template>
    <core-container>

        <spacer-item/>

        <core-content>
            <core-panel>
                <h1>{{ newJob ? 'New Job' : 'Edit Job'}}</h1>

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
                        </td>
                        <td>
                            <div v-if="triggerValidationError.length > 0" class="validation-error">
                                {{triggerValidationError}}
                            </div>
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

                <button-row>
                    <p slot="right">
                        <input-button v-on:clicked="addTask()">
                            <font-awesome-icon icon="plus"/>
                            Add Task
                        </input-button>
                    </p>
                </button-row>

            </core-panel>

            <task-editor v-for="(task, index) in jobConfiguration.tasks"
                         v-bind:task="task"
                         v-bind:index="index"
                         v-bind:key="task"/>

            <core-panel>
                <feedback-panel :feedback="feedback" :feedbackOk="feedbackOk" :requestInProgress="requestInProgress"/>

                <button-row>
                    <p slot="left">
                        <input-button v-on:clicked="cancel()">
                            <font-awesome-icon icon="times"/>
                        </input-button>
                    </p>

                    <p slot="right">
                        <input-button class="margin-right" v-on:clicked="testConfiguration()">
                            <font-awesome-icon icon="plug"/>
                        </input-button>

                        <input-button v-on:clicked="saveConfiguration()">
                            <font-awesome-icon icon="save"/>
                        </input-button>
                    </p>
                </button-row>

            </core-panel>
        </core-content>

        <spacer-item/>

    </core-container>
</template>

<script>
import TaskEditor from './task-editor'
import SpacerItem from '../common/spacer-item'
import CorePanel from '../common/core-panel'
import AlertBox from '../common/alert-box'
import InfoBox from '../common/info-box'
import InputButton from '../common/input-button'
import ServicePicker from '../services/service-picker'
import CoreContainer from '../common/core-container'
import CoreContent from '../common/core-content'
import ButtonRow from '../common/button-row'
import ValidationError from '../common/validation-error'
import FeedbackPanel from '../common/feedback-panel'

export default {
  name: 'job-editor',
  components: {
    FeedbackPanel,
    ValidationError,
    ButtonRow,
    CoreContent,
    CoreContainer,
    ServicePicker,
    CorePanel,
    InputButton,
    SpacerItem,
    InfoBox,
    AlertBox,
    TaskEditor
  },
  data: function () {
    return {
      newJob: true,
      jobId: '',
      feedback: '',
      feedbackOk: true,
      requestInProgress: false,
      nameValidationError: '',
      triggerValidationError: '',
      jobConfiguration: {
        id: '',
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
      this.feedback = 'Saving...'
      this.requestInProgress = true

      let component = this
      this.$http.post('/api/job', this.jobConfiguration).then(function () {
        component.feedback = 'Saved'
        component.feedbackOk = true
        component.requestInProgress = false
      }).catch(function (error) {
        component.feedback = error.response.data
        component.feedbackOk = false
        component.requestInProgress = false
      })
    },
    testConfiguration: function () {

    },
    addTask: function () {
      let task = {
        name: '',
        description: '',
        provider: {
          type: '',
          parameters: {}
        }
      }
      this.jobConfiguration.tasks.push(task)
    },
    validateInput: function () {

    },
    cancel: function () {
      this.$router.back()
    }
  },
  mounted () {
    this.jobId = this.$route.params.id
    if (this.jobId != null) {
      this.newJob = false
      this.showConfiguration = false
      this.loadJob(this.jobId)
    }
  }

}
</script>

<style scoped>

</style>
