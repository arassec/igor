<template>
    <modal-dialog v-if="wip.message.length > 0">
        <h1 slot="footer"><font-awesome-icon icon="spinner" class="fa-spin"/> {{wip.message}}</h1>
    </modal-dialog>
</template>

<script>
import ModalDialog from './modal-dialog'
export default {
  name: 'wip-window',
  components: {ModalDialog},
  computed: {
    wip: function () {
      return this.$root.$data.store.getWip()
    }
  }
}
</script>

<style scoped>

    h1 {
        margin-bottom: 0px;
    }

</style>
