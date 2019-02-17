<template>
    <modal-dialog>
        <p slot="header">Job Execution Details</p>

        <div slot="body">
            <table>
                <tr>
                    <td>Created</td>
                    <td>{{jobExecution.created}}</td>
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
export default {
  name: 'job-execution-details',
  props: ['jobExecution'],
  components: {ModalDialog}
}
</script>

<style scoped>

</style>
