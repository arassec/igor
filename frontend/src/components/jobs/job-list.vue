<template>
    <core-content>
        <list-header :addButtonTarget="'job-editor'" :addButtonText="'Add Job'" :filter="filter"
                     :filter-key="'job-list-filter'">
            <p slot="title">
                <font-awesome-icon icon="toolbox"/>
                Available Jobs
            </p>
        </list-header>

        <list-entry v-for="job in filteredJobs" :key="job.id" v-on:clicked="editJob(job.id)">
            <list-name slot="left" :class="!job.active ? 'inactive' : ''">
                {{ formatName(job.name) }}
            </list-name>
            <p slot="right" :class="!job.active ? 'inactive' : ''">
                <input-button v-on:clicked="duplicateJob(job.id)" icon="clone" class="button-margin-right"/>
                <input-button v-on:clicked="openDeleteJobDialog(job.id, job.name)"  class="button-margin-right" icon="trash-alt"/>
                <input-button v-on:clicked="openRunJobDialog(job.id, job.name)" icon="play"/>
            </p>
        </list-entry>

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

  export default {
    name: "job-list",
    components: {LayoutRow, ModalDialog, InputButton, DeleteJobDialog, ListName, ListEntry, ListHeader, CoreContent},
    data: function () {
      return {
        jobs: [],
        filterText: '',
        showDeleteDialog: false,
        showRunDialog: false,
        selectedJobId: null,
        selectedJobName: null
      }
    },
    methods: {
      loadJobs: function () {
        IgorBackend.getData('/api/job').then((result) => {
          for (let i = this.jobs.length; i > 0; i--) {
            this.jobs.pop()
          }
          let component = this
          Array.from(result).forEach(function (item) {
            component.jobs.push(item)
          })
          this.jobs.sort((a, b) => a.name.localeCompare(b.name))
        })
      },
      filter: function (filterTextFromListHeader) {
        this.filterText = filterTextFromListHeader
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
          this.loadJobs()
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
        return FormatUtils.formatNameForListEntry(name, 36)
      }
    },
    computed: {
      filteredJobs: function () {
        let component = this
        return this.jobs.filter(function (job) {
          return job.name.toLowerCase().includes(component.filterText.toLowerCase())
        })
      }
    },
    mounted() {
      this.loadJobs()
      this.$root.$on('reload-jobs', () => {
        this.loadJobs();
      });
    }
  }
</script>

<style scoped>

</style>
