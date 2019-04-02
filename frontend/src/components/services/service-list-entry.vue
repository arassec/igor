<template>
    <core-panel>

        <button-row>
            <list-name slot="left">
                {{ name }}
            </list-name>
            <p slot="right">
                <input-button v-on:clicked="duplicateService(id)" icon="clone" class="button-margin-right"/>
                <input-button v-on:clicked="showDeleteDialog = !showDeleteDialog" icon="trash-alt"
                              class="button-margin-right"/>
                <input-button v-on:clicked="editService(id)" icon="cog"/>
            </p>
        </button-row>

        <delete-service-dialog v-if="showDeleteDialog"
                               v-bind:service-id="id" v-bind:service-name="name"
                               v-on:cancel="showDeleteDialog = false"
                               v-on:delete-plus="deleteService(id, true)"
                               v-on:delete="deleteService(id, false)"/>

    </core-panel>
</template>

<script>
  import InputButton from '../common/input-button'
  import CorePanel from '../common/core-panel'
  import ButtonRow from '../common/button-row'
  import ListName from '../common/list-name'
  import DeleteServiceDialog from "./delete-service-dialog";

  export default {
    name: 'service-list-entry',
    components: {DeleteServiceDialog, ListName, ButtonRow, CorePanel, InputButton},
    props: ['id', 'name'],
    data: function () {
      return {
        showDeleteDialog: false
      }
    },
    methods: {
      editService: function (id) {
        this.$router.push({name: 'service-editor', params: {serviceId: id}})
      },
      deleteService: function (id, deleteAffectedJobs) {
        this.showDeleteDialog = false
        let component = this
        this.$http.delete('/api/service/' + id + '?deleteAffectedJobs=' + deleteAffectedJobs).then(function () {
          component.$root.$data.store.setFeedback('Service \'' + component.name + '\' has been deleted.', false)
          component.$emit('actionPerformed')
        }).catch(function (error) {
          component.$root.$data.store.setFeedback('Service \'' + component.name + '\' could not be deleted (' + error + ')', true)
          component.$emit('actionPerformed')
        })
      },
      duplicateService: function (id) {
        let component = this
        this.$http.get('/api/service/' + id).then(function (result) {
          let serviceConfiguration = result.data
          serviceConfiguration.name = 'Copy of ' + serviceConfiguration.name
          delete serviceConfiguration.id
          component.$root.$data.store.setServiceData(serviceConfiguration)
          component.$router.push({name: 'service-editor'})
        }).catch(function (error) {
          component.$root.$data.store.setFeedback('Service \'' + component.name + '\' could not be cloned (' + error + ')', true)
        })
      }
    }
  }
</script>

<style scoped>

</style>
