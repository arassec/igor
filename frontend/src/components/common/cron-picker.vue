<template>
  <modal-dialog>

      <layout-row slot="header">
          <h1 slot="left">Example CRON Expressions</h1>
          <input-button slot="right" icon="times" v-on:clicked="$emit('cancel')"/>
      </layout-row>

    <div slot="body">
        <feedback-box :clickable="true" v-on:feedback-clicked="$emit('selected', '0 0 * * * *')" :align-left="true">
            <div slot="left" class="cron-expression">"0 0 * * * *"</div>
            <div slot="right">The top of every hour of every day.</div>
        </feedback-box>

        <feedback-box :clickable="true" v-on:feedback-clicked="$emit('selected', '*/30 * * * * *')" :align-left="true">
            <div slot="left" class="cron-expression">"*/30 * * * * *"</div>
            <div slot="right">Every thirty seconds.</div>
        </feedback-box>

        <feedback-box :clickable="true" v-on:feedback-clicked="$emit('selected', '0 */15 * * * *')" :align-left="true"
            data-e2e="every-fiveteen-minutes-cron">
            <div slot="left" class="cron-expression">"0 */15 * * * *"</div>
            <div slot="right">Once every fifteen minutes.</div>
        </feedback-box>

        <feedback-box :clickable="true" v-on:feedback-clicked="$emit('selected', '0 0 8,10 * * *')" :align-left="true">
            <div slot="left" class="cron-expression">"0 0 8,10 * * *"</div>
            <div slot="right">8 and 10 o'clock of every day.</div>
        </feedback-box>

        <feedback-box :clickable="true" v-on:feedback-clicked="$emit('selected', '0 0/30 8-10 * * *')" :align-left="true">
            <div slot="left" class="cron-expression">"0 0/30 8-10 * * *"</div>
            <div slot="right">8:00, 8:30, 9:00, 9:30 and 10 o'clock every day.</div>
        </feedback-box>

        <feedback-box :clickable="true" v-on:feedback-clicked="$emit('selected', '0 0 9-17 * * MON-FRI')" :align-left="true">
            <div slot="left" class="cron-expression">"0 0 9-17 * * MON-FRI"</div>
            <div slot="right">On the hour nine-to-five weekdays.</div>
        </feedback-box>

        <feedback-box :clickable="true" v-on:feedback-clicked="$emit('selected', '0 0 0 25 12 ?')" :align-left="true">
            <div slot="left" class="cron-expression">"0 0 0 25 12 ?"</div>
            <div slot="right">Every Christmas Day at midnight.</div>
        </feedback-box>
    </div>

  </modal-dialog>
</template>

<script>
import ModalDialog from './modal-dialog'
import InputButton from './input-button'
import LayoutRow from './layout-row'
import FeedbackBox from "./feedback-box";

export default {
  name: 'cron-picker',
    components: {FeedbackBox, LayoutRow, InputButton, ModalDialog}
}
</script>

<style scoped>

    .cron-expression {
        min-width: 250px;
  }

</style>
