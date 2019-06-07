<template>
    <core-container>

        <side-menu class="side-menu-large">
            <p slot="title">Job Slots</p>
            <!--
            <layout-row slot="header">
                <p slot="left">
                    <input-button :icon="'clipboard-list'"/>
                </p>
            </layout-row>
            -->
            <p slot="content" v-if="runningJobs.length">
                <label class="list-label">Currently running jobs ({{this.runningJobs.length}}/{{this.numSlots}})</label>
                <feedback-box v-for="(runningJob, index) in runningJobs" :key="index" class="list-entry" :clickable="true"
                              v-on:feedback-clicked="openExecutionDetailsDialog(runningJob)">
                    <div slot="left">{{formatName(runningJob.jobName)}}</div>
                    <div slot="right">
                        {{runningJob.duration}}
                        <input-button slot="right" icon="times" v-on:clicked="openCancelJobDialog(runningJob)"/>
                    </div>
                </feedback-box>
            </p>
            <label slot="content" v-if="runningJobs.length === 0">No jobs are currently running.</label>
            <p slot="footer" v-if="waitingJobs.length">
                <label class="list-label">Waiting jobs</label>
                <feedback-box v-for="(waitingJob, index) in waitingJobs" :key="index" class="list-entry" :clickable="true"
                              v-on:feedback-clicked="openExecutionDetailsDialog(waitingJob)">
                    <div slot="left">{{formatName(waitingJob.jobName)}}</div>
                    <div slot="right">
                        {{waitingJob.duration}}
                        <input-button slot="right" icon="times" v-on:clicked="openCancelJobDialog(waitingJob)"/>
                    </div>
                </feedback-box>
            </p>
            <label slot="footer" v-if="waitingJobs.length === 0">No jobs are currently waiting.</label>
        </side-menu>

        <div class="column-container" style="display: flex; flex-grow: 1;">
            <div class="column" style="width: 50%;">
                <job-list class="core-content-normal"
                          :running-jobs="runningJobs"
                          :waiting-jobs="waitingJobs"/>
            </div>

            <div class="column" style="width: 50%">
                <service-list class="core-content-normal"/>
            </div>
        </div>

        <job-execution-details v-if="showExecutionDetailsDialog"
                               v-bind:job-execution="selectedJobExecution"
                               v-on:close="closeExecutionDetailsDialog()"/>

        <modal-dialog v-if="showCancelJobDialog"
                      @close="showCancelJobDialog = false"
                      v-on:cancel="showCancelJobDialog = false">
            <h1 slot="header">Cancel job execution</h1>
            <p slot="body">
                Are you sure you want to cancel this execution of job '{{selectedJobExecutionListEntry.jobName}}'?
            </p>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showCancelJobDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="cancelJobExecution()" icon="check"/>
            </layout-row>
        </modal-dialog>

        <background-icon right="true" icon-one="clipboard-list"/>

    </core-container>
</template>

<script>
  import CoreContainer from "../components/common/core-container";
  import SideMenu from "../components/common/side-menu";
  import LayoutRow from "../components/common/layout-row";
  import JobList from "../components/jobs/job-list";
  import ServiceList from "../components/services/service-list";
  import BackgroundIcon from "../components/common/background-icon";
  import InputButton from "../components/common/input-button";
  import FeedbackBox from "../components/common/feedback-box";
  import IgorBackend from '../utils/igor-backend.js'
  import FormatUtils from '../utils/format-utils.js'
  import ModalDialog from "../components/common/modal-dialog";
  import JobExecutionDetails from "../components/jobs/job-execution-details";

  export default {
    name: 'app-status',
    components: {
      JobExecutionDetails,
      ModalDialog,
      FeedbackBox, InputButton, BackgroundIcon, ServiceList, JobList, LayoutRow, SideMenu, CoreContainer
    },
    data: function () {
      return {
        numSlots: 0,
        runningJobs: [],
        waitingJobs: [],
        jobExecutionsListRefreshTimer: null,
        jobExecutionDetailsRefreshTimer: null,
        showCancelJobDialog: false,
        showExecutionDetailsDialog: false,
        selectedJobExecutionListEntry: null,
        selectedJobExecution: null,
        selectedJobExecutionId: null
      }
    },
    methods: {
      loadNumSlots: async function () {
        this.numSlots = await IgorBackend.getData('/api/execution/numSlots')
      },
      loadRunningJobs: function () {
        IgorBackend.getData('/api/execution/RUNNING').then((result) => {
          for (let i = this.runningJobs.length; i > 0; i--) {
            this.runningJobs.pop()
          }
          let component = this
          Array.from(result).forEach(function (item) {
            component.runningJobs.push(item)
          })
        })
      },
      loadWaitingJobs: function () {
        IgorBackend.getData('/api/execution/WAITING').then((result) => {
          for (let i = this.waitingJobs.length; i > 0; i--) {
            this.waitingJobs.pop()
          }
          let component = this
          Array.from(result).forEach(function (item) {
            component.waitingJobs.push(item)
          })
        })
      },
      openExecutionDetailsDialog: async function (selectedJobExecutionListEntry) {
        this.selectedJobExecutionId = selectedJobExecutionListEntry.id
        this.selectedJobExecution = await IgorBackend.getData('/api/execution/details/' + this.selectedJobExecutionId)
        this.showExecutionDetailsDialog = true
        this.jobExecutionDetailsRefreshTimer = setInterval(() => {
          this.updateJobExectuionDetails()
        }, 1000)
      },
      closeExecutionDetailsDialog: function () {
        clearTimeout(this.jobExecutionDetailsRefreshTimer)
        this.showExecutionDetailsDialog = false
      },
      updateJobExectuionDetails: function () {
        IgorBackend.getData('/api/execution/details/' + this.selectedJobExecutionId).then((result) => {
          this.selectedJobExecution = result
        })
      },
      openCancelJobDialog: function (selectedJobExecutionListEntry) {
        this.selectedJobExecutionListEntry = selectedJobExecutionListEntry
        this.showCancelJobDialog = true
      },
      cancelJobExecution: function () {
        this.showCancelJobDialog = false
        clearTimeout(this.jobExecutionsListRefreshTimer)
        IgorBackend.postData('/api/execution/' + this.selectedJobExecutionListEntry.id + '/cancel', null,
            "Cancelling job", "Job cancelled.", "Job could not be cancelled!").then(() => {
          for (let i = 0; i < this.runningJobs.length; i++) {
            if (this.runningJobs[i].id === this.selectedJobExecutionListEntry.id) {
              this.$delete(this.runningJobs, i)
              break
            }
          }
          this.jobExecutionsListRefreshTimer = setInterval(() => {
            this.loadNumSlots()
            this.loadRunningJobs()
            this.loadWaitingJobs()
          }, 1000)
        })
      },
      formatName: function (name) {
        return FormatUtils.shorten(name, 27)
      }
    },
    mounted() {
      this.$root.$data.store.clearServiceData()
      this.$root.$data.store.clearJobData()
      this.loadNumSlots().then(() => {
        this.loadRunningJobs()
        this.loadWaitingJobs()
        this.jobExecutionsListRefreshTimer = setInterval(() => {
          this.loadNumSlots()
          this.loadRunningJobs()
          this.loadWaitingJobs()
        }, 1000)
      })
    }
  }
</script>

<style scoped>

    .list-label {
        margin-bottom: 5px;
        display: inline-block;
    }

    .column-container {
        display: flex;
        flex-grow: 1;
    }

    .column {
        width: 50%;
    }

</style>
