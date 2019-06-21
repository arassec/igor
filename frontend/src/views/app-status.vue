<template>
    <core-container>

        <side-menu class="side-menu-large">
            <div slot="title">
                <div class="schedule-icon">
                    <font-awesome-icon icon="clipboard-list" v-on:click.stop="openScheduleDialog"/>
                </div>
                Job Slots
            </div>
            <p slot="header" v-if="runningJobsPage && runningJobsPage.items.length">
                <label class="list-label">Currently running jobs ({{this.runningOrWaitingJobs.length > this.numSlots ?
                    this.numSlots : this.runningOrWaitingJobs.length}}/{{this.numSlots}})</label>
                <feedback-box v-for="(runningJob, index) in runningJobsPage.items" :key="index" class="list-entry"
                              :clickable="true"
                              v-on:feedback-clicked="openExecutionDetailsDialog(runningJob)">
                    <div slot="left">{{formatName(runningJob.jobName, 27)}}</div>
                    <div slot="right">
                        {{runningJob.duration}}
                        <input-button slot="right" icon="times" v-on:clicked="openCancelJobDialog(runningJob)"/>
                    </div>
                </feedback-box>
                <list-pager :page="runningJobsPage" v-if="runningJobsPage.totalPages > 1"
                            v-on:first="manualLoadRunningJobs(0)"
                            v-on:previous="manualLoadRunningJobs(runningJobsPage.number -1)"
                            v-on:next="manualLoadRunningJobs(runningJobsPage.number + 1)"
                            v-on:last="manualLoadRunningJobs(runningJobsPage.totalPages - 1)"/>
            </p>
            <label slot="header" v-if="!runningJobsPage || runningJobsPage.items.length === 0">
                No jobs are currently running.</label>
            <p slot="content" v-if="waitingJobsPage && waitingJobsPage.items.length">
                <label class="list-label">Waiting jobs</label>
                <feedback-box v-for="(waitingJob, index) in waitingJobsPage.items" :key="index" class="list-entry"
                              :clickable="true"
                              v-on:feedback-clicked="openExecutionDetailsDialog(waitingJob)">
                    <div slot="left">{{formatName(waitingJob.jobName, 27)}}</div>
                    <div slot="right">
                        {{waitingJob.duration}}
                        <input-button slot="right" icon="times" v-on:clicked="openCancelJobDialog(waitingJob)"/>
                    </div>
                </feedback-box>
                <list-pager :page="waitingJobsPage" v-if="waitingJobsPage.totalPages > 1"
                            v-on:first="manualLoadWaitingJobs(0)"
                            v-on:previous="manualLoadWaitingJobs(waitingJobsPage.number -1)"
                            v-on:next="manualLoadWaitingJobs(waitingJobsPage.number + 1)"
                            v-on:last="manualLoadWaitingJobs(waitingJobsPage.totalPages - 1)"/>
            </p>
            <label slot="content" v-if="!waitingJobsPage || waitingJobsPage.items.length === 0">
                No jobs are currently waiting.</label>
            <p slot="footer" v-if="failedJobsPage && failedJobsPage.items.length">
                <label class="list-label">Failed jobs</label>
                <feedback-box v-for="(failedJob, index) in failedJobsPage.items" :key="index" class="list-entry"
                              :clickable="true" :alert="true"
                              v-on:feedback-clicked="openExecutionDetailsDialog(failedJob)">
                    <div slot="left">{{formatName(failedJob.jobName, 20)}}</div>
                    <div slot="right">
                        {{failedJob.finished}}
                        <input-button slot="right" icon="check" v-on:clicked="openMarkJobExecutionResolvedDialog(failedJob)"/>
                    </div>
                </feedback-box>
                <list-pager :page="failedJobsPage" v-if="failedJobsPage.totalPages > 1"
                            v-on:first="manualLoadFailedJobs(0)"
                            v-on:previous="manualLoadFailedJobs(failedJobsPage.number -1)"
                            v-on:next="manualLoadFailedJobs(failedJobsPage.number + 1)"
                            v-on:last="manualLoadFailedJobs(failedJobsPage.totalPages - 1)"/>
            </p>
            <label slot="footer" v-if="!failedJobsPage || failedJobsPage.items.length === 0">
                All jobs finished normally.</label>
        </side-menu>

        <div class="column-container" style="display: flex; flex-grow: 1;">
            <div class="column" style="width: 50%;">
                <job-list class="core-content-normal"
                          :running-or-waiting-jobs="runningOrWaitingJobs"/>
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

        <modal-dialog v-if="showMarkJobExecutionResolvedDialog"
                      @close="showMarkJobExecutionResolvedDialog = false"
                      v-on:cancel="showMarkJobExecutionResolvedDialog = false">
            <h1 slot="header">Mark job execution as resolved</h1>
            <div slot="body">
                <div class="paragraph">
                    Mark this failed execution as resolved?
                </div>
                <div class="paragraph" v-if="numFailedExecutionsForSelectedJob > 1">
                    Mark <b>all {{numFailedExecutionsForSelectedJob}}</b> executions of
                    job '{{formatName(selectedJobExecutionListEntry.jobName, 27)}}' as resolved:
                    <font-awesome-icon :icon="resolveAllFailedExecutionsOfJob ? 'check-square' : 'square'"
                                       v-on:click="resolveAllFailedExecutionsOfJob = !resolveAllFailedExecutionsOfJob"/>
                </div>
            </div>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showMarkJobExecutionResolvedDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="markJobExecutionResolved()" icon="check"/>
            </layout-row>
        </modal-dialog>

        <modal-dialog v-if="showScheduleDialog" @close="showScheduleDialog = false"
                      v-on:cancel="showScheduleDialog = false">
            <layout-row slot="header">
                <h1 slot="left">Scheduled jobs</h1>
                <input-button slot="right" icon="times" v-on:clicked="showScheduleDialog = false"/>
            </layout-row>

            <label slot="body" v-if="!schedulePage || !schedulePage.items || schedulePage.items.length === 0">
                There are currently no scheduled jobs.
            </label>

            <div class="schedule-box" slot="body">
                <feedback-box slot="body" v-for="scheduledJob in schedulePage.items" :key="scheduledJob.jobId" :clickable="true"
                              v-on:feedback-clicked="editJob(scheduledJob.jobId)">
                    <div slot="left" class="margin-right">{{formatName(scheduledJob.jobName, 27)}}</div>
                    <div slot="right">{{formatTimestamp(scheduledJob.nextRun)}}</div>
                </feedback-box>
            </div>

            <list-pager slot="footer" :page="schedulePage" v-on:first="loadSchedule(0)"
                        v-on:previous="loadSchedule(schedulePage.number -1)"
                        v-on:next="loadSchedule(schedulePage.number + 1)"
                        v-on:last="loadSchedule(schedulePage.totalPages -1)"/>
        </modal-dialog>

    </core-container>
</template>

<script>
  import CoreContainer from "../components/common/core-container";
  import SideMenu from "../components/common/side-menu";
  import LayoutRow from "../components/common/layout-row";
  import JobList from "../components/jobs/job-list";
  import ServiceList from "../components/services/service-list";
  import InputButton from "../components/common/input-button";
  import FeedbackBox from "../components/common/feedback-box";
  import IgorBackend from '../utils/igor-backend.js'
  import FormatUtils from '../utils/format-utils.js'
  import ModalDialog from "../components/common/modal-dialog";
  import JobExecutionDetails from "../components/jobs/job-execution-details";
  import ListPager from "../components/common/list-pager";

  export default {
    name: 'app-status',
    components: {
      ListPager,
      JobExecutionDetails,
      ModalDialog,
      FeedbackBox, InputButton, ServiceList, JobList, LayoutRow, SideMenu, CoreContainer
    },
    data: function () {
      return {
        numSlots: 0,
        runningOrWaitingJobs: [],
        runningJobsPage: {
          number: 0,
          size: 5,
          totalPages: 0,
          items: []
        },
        waitingJobsPage: {
          number: 0,
          size: 5,
          totalPages: 0,
          items: []
        },
        failedJobsPage: {
          number: 0,
          size: 10,
          totalPages: 0,
          items: []
        },
        schedulePage: {
          number: 0,
          size: 10,
          totalPages: 0,
          items: []
        },
        jobExecutionsListRefreshTimer: null,
        jobExecutionDetailsRefreshTimer: null,
        showCancelJobDialog: false,
        showExecutionDetailsDialog: false,
        showMarkJobExecutionResolvedDialog: false,
        showScheduleDialog: false,
        selectedJobExecutionListEntry: null,
        selectedJobExecution: null,
        selectedJobExecutionId: null,
        resolveAllFailedExecutionsOfJob: false,
        numFailedExecutionsForSelectedJob: 0
      }
    },
    methods: {
      loadNumSlots: async function () {
        this.numSlots = await IgorBackend.getData('/api/execution/numSlots')
      },
      loadRunningOrWaitingJobs: function () {
        IgorBackend.getData('/api/execution/jobs?states=RUNNING,WAITING').then((data) => {
          this.runningOrWaitingJobs = data
        })
      },
      loadRunningJobs: async function () {
        this.runningJobsPage = await IgorBackend.getData('/api/execution/RUNNING?pageNumber=' + this.runningJobsPage.number +
            '&pageSize=' + this.runningJobsPage.size)
      },
      manualLoadRunningJobs: async function (page) {
        clearInterval(this.jobExecutionsListRefreshTimer)
        this.runningJobsPage = await IgorBackend.getData('/api/execution/RUNNING?pageNumber=' + page +
            '&pageSize=' + this.runningJobsPage.size)
        this.enableRefreshTimer()
      },
      loadWaitingJobs: async function () {
        this.waitingJobsPage = await IgorBackend.getData('/api/execution/WAITING?pageNumber=' + this.waitingJobsPage.number +
            '&pageSize=' + this.waitingJobsPage.size)
      },
      manualLoadWaitingJobs: async function (page) {
        clearInterval(this.jobExecutionsListRefreshTimer)
        this.waitingJobsPage = await IgorBackend.getData('/api/execution/WAITING?pageNumber=' + page +
            '&pageSize=' + this.waitingJobsPage.size)
        this.enableRefreshTimer()
      },
      loadFailedJobs: async function () {
        this.failedJobsPage = await IgorBackend.getData('/api/execution/FAILED?pageNumber=' + this.failedJobsPage.number +
            '&pageSize=' + this.failedJobsPage.size)
      },
      manualLoadFailedJobs: async function (page) {
        clearInterval(this.jobExecutionsListRefreshTimer)
        this.failedJobsPage = await IgorBackend.getData('/api/execution/FAILED?pageNumber=' + page +
            '&pageSize=' + this.failedJobsPage.size)
        this.enableRefreshTimer()
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
        let component = this
        IgorBackend.postData('/api/execution/' + this.selectedJobExecutionListEntry.id + '/cancel', null,
            "Cancelling job", "Job cancelled.", "Job could not be cancelled!").then(() => {
          let running = false
          let waiting = false
          for (let i = 0; i < component.runningJobsPage.items.length; i++) {
            if (component.runningJobsPage.items[i].id === component.selectedJobExecutionListEntry.id) {
              running = true
              component.$delete(component.runningJobsPage.items, i)
              break
            }
          }
          for (let i = 0; i < component.waitingJobsPage.items.length; i++) {
            if (component.waitingJobsPage.items[i].id === component.selectedJobExecutionListEntry.id) {
              component.$delete(component.waitingJobsPage.items, i)
              waiting = true
              break
            }
          }
          if (running) {
            component.manualLoadRunningJobs(0).then(
                component.enableRefreshTimer()
            )
          } else if (waiting) {
            component.manualLoadWaitingJobs(0).then(
                component.enableRefreshTimer()
            )
          }
        })
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
        clearTimeout(this.jobExecutionsListRefreshTimer)
        await IgorBackend.putData('/api/execution/' + this.selectedJobExecutionListEntry.id + '/' +
            this.selectedJobExecutionListEntry.jobId + '/FAILED/RESOLVED?updateAllOfJob=' + this.resolveAllFailedExecutionsOfJob, null,
            'Updating executions', 'Executions updated', 'Executions could not be updated!')
        await this.manualLoadFailedJobs(0)
        this.resolveAllFailedExecutionsOfJob = false
        this.enableRefreshTimer()
        this.showMarkJobExecutionResolvedDialog = false
      },
      formatName: function (name, length) {
        return FormatUtils.shorten(name, length)
      },
      enableRefreshTimer: function () {
        this.jobExecutionsListRefreshTimer = setInterval(() => {
          this.loadNumSlots()
          this.loadRunningOrWaitingJobs()
          this.loadRunningJobs()
          this.loadWaitingJobs()
          this.loadFailedJobs()
        }, 1000)
      },
      openScheduleDialog: async function () {
        await this.loadSchedule(0)
        this.showScheduleDialog = true
      },
      loadSchedule: async function (page) {
        this.schedulePage = await IgorBackend.getData('/api/job/schedule?pageNumber=' + page + '&pageSize=' +
            this.schedulePage.size)
      },
      editJob: function (jobId) {
        this.$router.push({name: 'job-editor', params: {jobId: jobId}})
      },
      formatTimestamp: function (timestamp) {
        return FormatUtils.formatInstant(timestamp)
      }
    },
    mounted() {
      this.$root.$data.store.clearServiceData()
      this.$root.$data.store.clearJobData()
      this.loadNumSlots().then(() => {
        this.loadRunningOrWaitingJobs()
        this.loadRunningJobs()
        this.loadWaitingJobs()
        this.loadFailedJobs()
        this.enableRefreshTimer()
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

    .paragraph {
        margin-bottom: 20px;
    }

    .schedule-icon {
        width: 35px;
        float: left;
    }

    .schedule-icon:hover {
        font-size: 110%;
        cursor: pointer;
    }

    .schedule-box {
        min-height: 400px;
        min-width: 520px;
    }

    .margin-right {
        margin-right: 25px;
    }

</style>
