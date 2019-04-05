<template>
    <core-panel :class="'small-panel'" v-on:click="editJob(id)">

        <button-row>
            <list-name slot="left">
                {{ name }} <span v-if="!active">(inactive)</span>
            </list-name>
            <p slot="right">
                <input-button v-on:clicked="duplicateJob(id)" icon="clone" class="button-margin-right"/>
                <input-button v-on:clicked="showDeleteDialog = !showDeleteDialog" icon="trash-alt"
                              class="button-margin-right"/>
                <input-button v-on:clicked="editJob(id)" icon="cog"/>
            </p>
        </button-row>

        <delete-job-dialog v-if="showDeleteDialog"
                           v-bind:job-id="id" v-bind:job-name="name"
                           v-on:cancel="showDeleteDialog = false"
                           v-on:delete-plus="deleteJob(id, true)"
                           v-on:delete="deleteJob(id, false)"/>

    </core-panel>
</template>

<script>

  import InputButton from '../common/input-button'
  import ListName from '../common/list-name'
  import ButtonRow from '../common/button-row'
  import CorePanel from "../common/core-panel";
  import DeleteJobDialog from "./delete-job-dialog";

  export default {
    name: 'job-list-entry',
    components: {DeleteJobDialog, CorePanel, ButtonRow, ListName, InputButton},
    props: ['id', 'name', 'active'],
    data: function () {
      return {
        showDeleteDialog: false
      }
    },
    methods: {
      editJob: function (jobId) {
        this.$router.push({name: 'job-editor', params: {jobId: jobId}})
      },
      deleteJob: function (jobId, deleteExclusiveServices) {
        this.showDeleteDialog = false
        let component = this
        this.$http.delete('/api/job/' + jobId + '?deleteExclusiveServices=' + deleteExclusiveServices).then(function () {
          component.$root.$data.store.setFeedback('Job \'' + component.name + '\' has been deleted.', false)
          component.$emit('job-deleted')
        }).catch(function (error) {
          component.$root.$data.store.setFeedback('Job \'' + component.name + '\' could not be deleted (' + error + ')', true)
          component.$emit('job-deleted')
        })
      },
      duplicateJob: function (id) {
        let component = this
        this.$http.get('/api/job/' + id).then(function (result) {
          let jobConfiguration = result.data
          jobConfiguration.name = 'Copy of ' + jobConfiguration.name
          delete jobConfiguration.id
          for (let task in jobConfiguration.tasks) {
            delete task.id
          }
          component.$root.$data.store.setJobData(jobConfiguration, '-1_-1', -1, '')
          component.$router.push({name: 'job-editor'})
        }).catch(function (error) {
          component.$root.$data.store.setFeedback('Service \'' + component.name + '\' could not be cloned (' + error + ')', true)
        })
      }
    }
  }
</script>

<style scoped>

    .small-panel {
        padding: 5px 15px 5px 15px;
    }

</style>
