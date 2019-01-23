<template>
  <core-panel>

    <button-row>
      <list-name slot="left">
        {{ name }}
      </list-name>
      <p slot="right">
        <input-button v-on:clicked="showDeleteDialog = !showDeleteDialog" icon="trash-alt" class="button-margin-right"/>
        <input-button v-on:clicked="editService(id)" icon="cog"/>
      </p>
    </button-row>

    <modal-dialog v-if="showDeleteDialog" @close="showDeleteDialog = false">
      <p slot="header">Delete Service?</p>
      <p slot="body">Do you really want to delete service '{{name}}'?</p>
      <div slot="footer">
        <button-row>
          <input-button slot="left" v-on:clicked="showDeleteDialog = false" icon="times"/>
          <input-button slot="right" v-on:clicked="deleteService(id)" icon="check"/>
        </button-row>
      </div>
    </modal-dialog>

  </core-panel>
</template>

<script>
import ModalDialog from '../common/modal-dialog'
import InputButton from '../common/input-button'
import CorePanel from '../common/core-panel'
import ButtonRow from '../common/button-row'
import ListName from '../common/list-name'

export default {
  name: 'service-list-entry',
  components: {ListName, ButtonRow, CorePanel, InputButton, ModalDialog},
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
    deleteService: function (id) {
      this.showDeleteDialog = false
      let component = this
      this.$http.delete('/api/service/' + id).then(function () {
        component.$root.$data.store.setFeedback('Service \'' + component.name + '\' has been deleted.', false)
        component.$emit('actionPerformed')
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Service \'' + component.name + '\' could not be deleted (' + error + ')', true)
        component.$emit('actionPerformed')
      })
    }
  }
}
</script>

<style scoped>

</style>
