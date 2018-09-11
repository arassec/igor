<template>

    <core-panel>

        <button-row>
            <list-name slot="left">
                {{ name }}
            </list-name>
            <p slot="right">
                <input-button v-on:clicked="editService(id)" class="margin-right">
                    <font-awesome-icon icon="cog"/>
                </input-button>

                <input-button v-on:clicked="showDeleteDialog = !showDeleteDialog">
                    <font-awesome-icon icon="trash-alt"/>
                </input-button>
            </p>
        </button-row>

        <modal-dialog v-if="showDeleteDialog" @close="showDeleteDialog = false">
            <p slot="header">Delete Service?</p>
            <p slot="body">Do you really want to delete service '{{name}}'?</p>
            <div slot="footer">
                <button-row>
                    <p slot="left">
                        <input-button v-on:clicked="showDeleteDialog = false">
                            <font-awesome-icon icon="times"/>
                        </input-button>
                    </p>
                    <p slot="right">
                        <input-button class="right" v-on:clicked="deleteService(id)">
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
      showDeleteDialog: false,
      showEditService: false,
      requestInProgress: false,
      serviceConfiguration: {}
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
        component.$emit('actionPerformed', "Service '" + component.name + "' has been deleted.", true)
      }).catch(function (error) {
        console.log(error)
        component.$emit('actionPerformed', "Service '" + component.name + "' could not be deleted!", false)
      })
    }
  }
}
</script>

<style scoped>

</style>
