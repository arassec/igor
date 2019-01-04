<template>
  <modal-dialog>

    <p slot="header">Example CRON Expressions</p>

    <div slot="body">
      <table>
        <tr>
          <td>"0 0 * * * *"</td>
          <td>The top of every hour of every day.</td>
          <td class="last">
            <input-button v-on:clicked="$emit('selected', '0 0 * * * *')" icon="crosshairs"/>
          </td>
        </tr>
        <tr>
          <td>"*/10 * * * * *"</td>
          <td>Every ten seconds.</td>
          <td class="last">
            <input-button v-on:clicked="$emit('selected', '*/10 * * * * *')" icon="crosshairs"/>
          </td>
        </tr>
        <tr>
          <td>"0 */15 * * * *"</td>
          <td>Once every fifteen minutes.</td>
          <td class="last">
            <input-button v-on:clicked="$emit('selected', '0 */15 * * * *')" icon="crosshairs"/>
          </td>
        </tr>
        <tr>
          <td>"0 0 8,10 * * *"</td>
          <td>8 and 10 o'clock of every day.</td>
          <td class="last">
            <input-button v-on:clicked="$emit('selected', '0 0 8,10 * * *')" icon="crosshairs"/>
          </td>
        </tr>
        <tr>
          <td>"0 0/30 8-10 * * *"</td>
          <td>8:00, 8:30, 9:00, 9:30 and 10 o'clock every day.</td>
          <td class="last">
            <input-button v-on:clicked="$emit('selected', '0 0/30 8-10 * * *')" icon="crosshairs"/>
          </td>
        </tr>
        <tr>
          <td>"0 0 9-17 * * MON-FRI"</td>
          <td>On the hour nine-to-five weekdays</td>
          <td class="last">
            <input-button v-on:clicked="$emit('selected', '0 0 9-17 * * MON-FRI')" icon="crosshairs"/>
          </td>
        </tr>
        <tr>
          <td>"0 0 0 25 12 ?"</td>
          <td>Every Christmas Day at midnight.</td>
          <td class="last">
            <input-button v-on:clicked="$emit('selected', '0 0 0 25 12 ?')" icon="crosshairs"/>
          </td>
        </tr>
      </table>
    </div>

    <button-row slot="footer">
      <input-button slot="left" icon="times" v-on:clicked="$emit('cancel')"/>
    </button-row>

  </modal-dialog>
</template>

<script>
import ModalDialog from './modal-dialog'
import InputButton from './input-button'
import ButtonRow from './button-row'

export default {
  name: 'cron-picker',
  components: {ButtonRow, InputButton, ModalDialog}
}
</script>

<style scoped>

  .last {
    padding-right: 0px !important;
  }

</style>
