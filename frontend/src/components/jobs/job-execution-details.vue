<template>
    <modal-dialog>
        <p slot="header">
            <button-row>
                <h1 slot="left">Job Execution Details</h1>
                <input-button slot="right" icon="times" v-on:clicked="$emit('close')"/>
            </button-row>
        </p>

        <div slot="body">
            <h2>Time and state</h2>
            <table class="execution-core">
                <tr>
                    <td>State</td>
                    <td>{{jobExecution.executionState}}</td>
                </tr>
                <tr v-if="jobExecution.started == null">
                    <td>Scheduled:</td>
                    <td>{{formatDate(jobExecution.created)}}</td>
                </tr>
                <tr v-if="jobExecution.started == null">
                    <td>Waiting:</td>
                    <td>{{calculateDuration(jobExecution.created, jobExecution.finished)}}</td>
                </tr>
                <tr v-if="jobExecution.started != null">
                    <td>Started:</td>
                    <td>{{formatDate(jobExecution.started)}}</td>
                </tr>
                <tr v-if="jobExecution.finished != null">
                    <td>Finished:</td>
                    <td>{{formatDate(jobExecution.finished)}}</td>
                </tr>
                <tr v-if="jobExecution.started != null">
                    <td>Duration:</td>
                    <td>{{calculateDuration(jobExecution.started, jobExecution.finished)}}</td>
                </tr>
            </table>

            <div v-if="jobExecution.errorCause != ''">
            <h2>Error cause</h2>
                <pre><code>{{ jobExecution.errorCause }}
      </code>
    </pre>
            </div>
        </div>

    </modal-dialog>
</template>

<script>
import ModalDialog from '../common/modal-dialog'
import ButtonRow from '../common/button-row'
import InputButton from '../common/input-button'

export default {
  name: 'job-execution-details',
  props: ['jobExecution'],
  components: {InputButton, ButtonRow, ModalDialog},
  methods: {
    formatDate: function (unformattedDate) {
      let options = { year: 'numeric', month: '2-digit', day: '2-digit' };
      let date = new Date(unformattedDate)
      return date.toLocaleDateString(undefined, options) + ' ' + date.toLocaleTimeString()
    },
    calculateDuration: function (from, till) {
      let start = new Date(from).getTime()
      let end = Date.now()
      if (till != null) {
        end = new Date(till).getTime()
      }
      let delta = Math.ceil((end - start) / 1000)
      let hours = Math.floor(delta / 3600) % 24
      delta -= hours * 3600
      let minutes = Math.floor(delta / 60) % 60
      delta -= minutes * 60
      let seconds = delta % 60
      return hours.toString().padStart(2, '0') + ':' + minutes.toString().padStart(2, '0') + ':' + seconds.toString().padStart(2, '0')
    }
  }
}
</script>

<style scoped>

    .execution-core {
        margin-bottom: 30px;
    }

    pre {
        max-height: calc(100vh / 2);
        height: auto;
        overflow: auto;
        word-break: normal !important;
        word-wrap: normal !important;
        white-space: pre !important;
        background-color: var(--info-background-color)
    }

</style>
