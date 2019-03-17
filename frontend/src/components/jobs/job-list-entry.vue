<template>
  <core-panel>

    <button-row>
      <list-name slot="left">
        {{ name }} <span v-if="!active">(inactive)</span>
      </list-name>
      <p slot="right">
        <input-button v-on:clicked="showDeleteDialog = !showDeleteDialog" icon="trash-alt" class="button-margin-right"/>
        <input-button v-on:clicked="editJob(id)" icon="cog"/>
      </p>
    </button-row>

    <modal-dialog v-if="showDeleteDialog">
      <h1 slot="header">Delete Job?</h1>
      <p slot="body">Do you really want to delete job '{{name}}'?</p>
      <div slot="footer">
        <button-row>
          <input-button slot="left" v-on:clicked="showDeleteDialog = false" icon="times"/>
          <input-button slot="right" v-on:clicked="deleteJob(id)" icon="check"/>
        </button-row>
      </div>
    </modal-dialog>

  </core-panel>
</template>

<script>

import ModalDialog from '../common/modal-dialog'
import InputButton from '../common/input-button'
import ListName from '../common/list-name'
import ButtonRow from '../common/button-row'
import CorePanel from "../common/core-panel";

export default {
  name: 'job-list-entry',
  components: {CorePanel, ButtonRow, ListName, InputButton, ModalDialog},
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
    deleteJob: function (jobId) {
      this.showDeleteDialog = false
      let component = this
      this.$http.delete('/api/job/' + jobId).then(function () {
        component.$root.$data.store.setFeedback('Job \'' + component.name + '\' has been deleted.', false)
        component.$emit('job-deleted')
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Job \'' + component.name + '\' could not be deleted (' + error + ')', true)
        component.$emit('job-deleted')
      })
    }
  }
}
</script>

<style scoped>

</style>
