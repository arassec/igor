<template>
  <modal-dialog>

    <h1 slot="header">Select Service</h1>

    <div slot="body">
        <table>
          <tr v-if="services == null || services.length === 0">
            <td>No service of the required category available. Please create a new service with the button below.</td>
          </tr>
          <tr v-for="service in services" :key="service.id">
            <td class="first">
              {{ service.name }}
            </td>
            <td class="last">
              <input-button icon="crosshairs" v-on:clicked="$emit('selected', service)"/>
            </td>
          </tr>
        </table>
    </div>

    <button-row slot="footer">
      <input-button slot="left" icon="times" v-on:clicked="$emit('cancel')"/>
      <input-button slot="right" icon="plus" v-on:clicked="$emit('create')"/>
    </button-row>

  </modal-dialog>
</template>

<script>
import ModalDialog from '../common/modal-dialog'
import ButtonRow from '../common/button-row'
import InputButton from '../common/input-button'

export default {
  name: 'service-picker',
  components: {InputButton, ButtonRow,  ModalDialog},
  props: ['services'],
  data: function () {
    return {
      feedback: '',
      feedbackOk: true
    }
  }
}
</script>

<style scoped>

  table {
    width: 100%;
  }

  .first {
    width: 100%;
  }

  .last {
    padding-right: 0px !important;
  }

</style>
