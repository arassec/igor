<template>

    <core-panel>

        <button-row>
            <list-name slot="left">
                {{ name }}
            </list-name>
            <p slot="right">
                <input-button v-on:clicked="editJob(id)" class="margin-right">
                    <font-awesome-icon icon="cog"/>
                </input-button>

                <input-button v-on:clicked="showDeleteDialog = !showDeleteDialog">
                    <font-awesome-icon icon="trash-alt"/>
                </input-button>
            </p>
        </button-row>

        <modal-dialog v-if="showDeleteDialog">
            <p slot="header">Delete Job?</p>
            <p slot="body">Do you really want to delete job '{{name}}'?</p>
            <div slot="footer">
                <button-row>
                    <p slot="left">
                        <input-button v-on:clicked="showDeleteDialog = false">
                            <font-awesome-icon icon="times"/>
                        </input-button>
                    </p>
                    <p slot="right">
                        <input-button class="right" v-on:clicked="deleteJob(id)">
                            <font-awesome-icon icon="check"/>
                        </input-button>
                    </p>
                </button-row>
            </div>
        </modal-dialog>

    </core-panel>
</template>

<script>

import ModalDialog from '../common/modal-dialog'
export default {
  name: 'job-list-entry',
  components: {ModalDialog},
  props: ['id', 'name'],
  data: function () {
    return {
      showDeleteDialog: false,
      feedback: '',
      feedbackOk: true
    }
  },
  methods: {
    editJob: function (jobId) {
      this.$router.push({name: 'editor', params: {id: jobId}})
    },
    deleteJob: function (jobId) {
      this.feedback = ''
      this.feedbackOk = true
      this.showDeleteDialog = false
      let component = this
      this.$http.delete('/api/job/' + jobId).then(function () {
        component.$emit('job-deleted')
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    }
  }
}
</script>

<style scoped>

</style>
