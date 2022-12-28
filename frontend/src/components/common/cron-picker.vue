<template>
    <modal-dialog>
        <template v-slot:header>
            <layout-row>
                <template v-slot:left>
                    <h1>Example CRON Expressions</h1>
                </template>
                <template v-slot:right>
                    <input-button icon="times" v-on:clicked="$emit('cancel')" />
                </template>
            </layout-row>
        </template>

        <template v-slot:body>
            <feedback-box :clickable="true" v-on:feedback-clicked="$emit('selected', '0 0 * * * *')" :align-left="true">
                <template v-slot:left>
                    <div class="cron-expression">"0 0 * * * *"</div>
                </template>
                <template v-slot:right>
                    <div>The top of every hour of every day.</div>
                </template>
            </feedback-box>

            <feedback-box
                :clickable="true"
                v-on:feedback-clicked="$emit('selected', '*/30 * * * * *')"
                :align-left="true"
            >
                <template v-slot:left>
                    <div class="cron-expression">"*/30 * * * * *"</div>
                </template>
                <template v-slot:right>
                    <div>Every thirty seconds.</div>
                </template>
            </feedback-box>

            <feedback-box
                :clickable="true"
                v-on:feedback-clicked="$emit('selected', '0 */15 * * * *')"
                :align-left="true"
                data-e2e="every-fiveteen-minutes-cron"
            >
                <template v-slot:left>
                    <div class="cron-expression">"0 */15 * * * *"</div>
                </template>
                <template v-slot:right>
                    <div>Once every fifteen minutes.</div>
                </template>
            </feedback-box>

            <feedback-box
                :clickable="true"
                v-on:feedback-clicked="$emit('selected', '0 0 8,10 * * *')"
                :align-left="true"
            >
                <template v-slot:left>
                    <div class="cron-expression">"0 0 8,10 * * *"</div>
                </template>
                <template v-slot:right>
                    <div>8 and 10 o'clock of every day.</div>
                </template>
            </feedback-box>

            <feedback-box
                :clickable="true"
                v-on:feedback-clicked="$emit('selected', '0 0/30 8-10 * * *')"
                :align-left="true"
            >
                <template v-slot:left>
                    <div class="cron-expression">"0 0/30 8-10 * * *"</div>
                </template>
                <template v-slot:right>
                    <div>8:00, 8:30, 9:00, 9:30 and 10 o'clock every day.</div>
                </template>
            </feedback-box>

            <feedback-box
                :clickable="true"
                v-on:feedback-clicked="$emit('selected', '0 0 9-17 * * MON-FRI')"
                :align-left="true"
            >
                <template v-slot:left>
                    <div class="cron-expression">"0 0 9-17 * * MON-FRI"</div>
                </template>
                <template v-slot:right>
                    <div>On the hour nine-to-five weekdays.</div>
                </template>
            </feedback-box>

            <feedback-box
                :clickable="true"
                v-on:feedback-clicked="$emit('selected', '0 0 0 25 12 ?')"
                :align-left="true"
            >
                <template v-slot:left>
                    <div class="cron-expression">"0 0 0 25 12 ?"</div>
                </template>
                <template v-slot:right>
                    <div>Every Christmas Day at midnight.</div>
                </template>
            </feedback-box>
        </template>
    </modal-dialog>
</template>

<script>
import ModalDialog from "./modal-dialog.vue";
import InputButton from "./input-button.vue";
import LayoutRow from "./layout-row.vue";
import FeedbackBox from "./feedback-box.vue";

export default {
    name: "cron-picker",
    components: { FeedbackBox, LayoutRow, InputButton, ModalDialog },
};
</script>

<style scoped>
.cron-expression {
    min-width: 250px;
}
</style>
