<template>
    <core-content>
        <list-header :addButtonTarget="'job-editor'" :addButtonText="'Add Job'" :filter="filter"
                     :filter-key="'job-list-filter'">
            <p slot="title">
                <font-awesome-icon icon="toolbox"/>
                Available Jobs
            </p>
        </list-header>

        <div v-if="jobsPage">
            <list-entry v-for="job in jobsPage.items" :key="job.id" v-on:clicked="editJob(job.id)">
                <list-name slot="left" :class="!job.active ? 'inactive' : ''">
                    {{ formatName(job.name) }}
                </list-name>
                <p slot="right" :class="!job.active ? 'inactive' : ''">
                    <input-button v-on:clicked="duplicateJob(job.id)" icon="clone" class="button-margin-right"/>
                    <input-button v-on:clicked="openDeleteJobDialog(job.id, job.name)" class="button-margin-right"
                                  icon="trash-alt"/>
                    <input-button v-on:clicked="openRunJobDialog(job.id, job.name)" icon="play"
                                  :disabled="jobRunningOrWaiting(job.id) || !job.active"/>
                </p>
            </list-entry>

            <list-pager :page="jobsPage" v-if="jobsPage && jobsPage.totalPages > 1" :dark="true"
                        v-on:first="loadJobs(0)"
                        v-on:previous="loadJobs(jobsPage.number - 1)"
                        v-on:next="loadJobs(jobsPage.number + 1)"
                        v-on:last="loadJobs(jobsPage.totalPages -1)"/>
        </div>

        <delete-job-dialog v-if="showDeleteDialog"
                           v-bind:job-id="selectedJobId"
                           v-bind:job-name="selectedJobName"
                           v-on:cancel="showDeleteDialog = false"
                           v-on:delete-plus="deleteJob(true)"
                           v-on:delete="deleteJob(false)"/>

        <modal-dialog v-if="showRunDialog"
                      @close="showRunDialog = false"
                      v-on:cancel="showRunDialog = false">
            <h1 slot="header">Start job</h1>
            <p slot="body">
                Manually start job '{{selectedJobName}}' now?
            </p>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showRunDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="runJob()" icon="check"/>
            </layout-row>
        </modal-dialog>

    </core-content>
</template>

<script>
  import CoreContent from "../common/core-content";
  import ListHeader from "../common/list-header";
  import ListEntry from "../common/list-entry";
  import ListName from "../common/list-name";
  import DeleteJobDialog from "./delete-job-dialog";
  import IgorBackend from '../../utils/igor-backend.js'
  import FormatUtils from '../../utils/format-utils.js'
  import InputButton from "../common/input-button";
  import ModalDialog from "../common/modal-dialog";
  import LayoutRow from "../common/layout-row";
  import ListPager from "../common/list-pager";

  export default {
    name: "job-list",
    components: {ListPager, LayoutRow, ModalDialog, InputButton, DeleteJobDialog, ListName, ListEntry, ListHeader, CoreContent},
    props: ['runningOrWaitingJobs'],
    data: function () {
      return {
        jobsPage: {
          number: 0,
          size: 12,
          totalPages: 0,
          items: []
        },
        filterText: '',
        showDeleteDialog: false,
        showRunDialog: false,
        selectedJobId: null,
        selectedJobName: null
      }
    },
    methods: {
      jobRunningOrWaiting: function (jobId) {
        if (this.runningOrWaitingJobs != null) {
          for (let i = 0; i < this.runningOrWaitingJobs.length; i++) {
            if (jobId === this.runningOrWaitingJobs[i]) {
              return true
            }
          }
        }
        return false
      },
      loadJobs: async function (page) {
        if (this.jobsPage) {
          this.jobsPage = await IgorBackend.getData('/api/job?pageNumber=' + page + '&pageSize=' + this.jobsPage.size +
              "&nameFilter=" + this.filterText);
        }
      },
      filter: function (filterTextFromListHeader) {
        this.filterText = filterTextFromListHeader
        this.loadJobs(0)
      },
      editJob: function (jobId) {
        this.$router.push({name: 'job-editor', params: {jobId: jobId}})
      },
      openDeleteJobDialog: function (jobId, jobName) {
        this.selectedJobId = jobId
        this.selectedJobName = jobName
        this.showDeleteDialog = true
      },
      openRunJobDialog: function (jobId, jobName) {
        this.selectedJobId = jobId
        this.selectedJobName = jobName
        this.showRunDialog = true
      },
      deleteJob: function (deleteExclusiveServices) {
        this.showDeleteDialog = false
        IgorBackend.deleteData('/api/job/' + this.selectedJobId + '?deleteExclusiveServices=' + deleteExclusiveServices,
            'Deleting job', 'Job \'' + FormatUtils.formatNameForSnackbar(this.selectedJobName) + '\' has been deleted.',
            'Job \'' + FormatUtils.formatNameForSnackbar(this.selectedJobName) + '\' could not be deleted!').then(() => {
          this.loadJobs(0)
          if (deleteExclusiveServices) {
            this.$root.$emit('reload-services')
          }
        })
      },
      duplicateJob: async function (id) {
        let jobConfiguration = await IgorBackend.getData('/api/job/' + id)
        jobConfiguration.name = 'Copy of ' + jobConfiguration.name
        delete jobConfiguration.id
        for (let i = 0; i < jobConfiguration.tasks.length; i++) {
          delete jobConfiguration.tasks[i].id
        }
        this.$root.$data.store.setJobData(jobConfiguration, '-1_-1', -1, '')
        this.$router.push({name: 'job-editor'})
      },
      runJob: function () {
        IgorBackend.postData('/api/job/run/' + this.selectedJobId, null, 'Starting job',
            'Job \'' + FormatUtils.formatNameForSnackbar(this.selectedJobName) + '\' started manually.',
            'Job \'' + FormatUtils.formatNameForSnackbar(this.selectedJobName) + '\' startup failed'
        ).then(() => {
          this.showRunDialog = false
        })
      },
      formatName: function (name) {
        return FormatUtils.shorten(name, 36)
      }
    },
    mounted() {
      if (this.$root.$data.store.getValue('job-list-filter')) {
        this.filterText = this.$root.$data.store.getValue('job-list-filter')
      }
      this.loadJobs(0)
      this.$root.$on('reload-jobs', () => {
        this.loadJobs(0);
      });
    }
  }
</script>

<style scoped>

</style>
