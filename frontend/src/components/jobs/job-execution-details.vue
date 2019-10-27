<template>
    <modal-dialog>
        <p slot="header">
            <layout-row>
                <h1 slot="left">Job Execution Details</h1>
                <input-button slot="right" icon="times" v-on:clicked="$emit('close')" class="button-margin-right"/>
            </layout-row>
        </p>

        <div class="" slot="body">
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

            <div v-if="jobExecution.executionState === 'RUNNING'" class="max-width">
                <h2 class="truncate">Task: {{jobExecution.currentTask}}</h2>

                <feedback-box v-for="(wip, index) in jobExecution.workInProgress" :key="index" :clickable="false">
                    <div slot="left" class="margin-right truncate">{{wip.name}}</div>
                    <div slot="right">{{formatPercent(wip.progressInPercent)}}</div>
                </feedback-box>
            </div>

            <div v-if="jobExecution.errorCause">
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
    import LayoutRow from '../common/layout-row'
    import InputButton from '../common/input-button'
    import FeedbackBox from '../common/feedback-box'

    export default {
    name: 'job-execution-details',
    props: ['jobExecution'],
    components: {FeedbackBox, InputButton, LayoutRow, ModalDialog},
    methods: {
      formatDate: function (unformattedDate) {
        let options = {year: 'numeric', month: '2-digit', day: '2-digit'};
        let date = new Date(unformattedDate)
        return date.toLocaleDateString(undefined, options) + ' ' + date.toLocaleTimeString()
      },
      formatPercent: function (input) {
        return input.toFixed(2) + '%'
      },
      calculateDuration: function (from, till) {
        let start = new Date(from).getTime()
        let end = Date.now()
        if (till != null) {
          end = new Date(till).getTime()
        }
        let delta = Math.floor((end - start) / 1000)
        let hours = Math.floor(delta / 3600) % 24
        delta -= hours * 3600
        let minutes = Math.floor(delta / 60) % 60
        delta -= minutes * 60
        let seconds = Math.floor(delta % 60)
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
        padding: 10px;
        max-height: calc(100vh / 2);
        height: auto;
        overflow: auto;
        word-break: normal !important;
        word-wrap: normal !important;
        white-space: pre !important;
        background-color: var(--alert-background-color)
    }

    .margin-right {
        margin-right: 25px;
    }

</style>
