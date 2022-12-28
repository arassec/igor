<template>
    <modal-dialog @close="$emit('close')" v-on:cancel="$emit('close')">
        <template v-slot:header>
            <layout-row>
                <template v-slot:left>
                    <h1>Job Execution Details</h1>
                </template>
                <template v-slot:right>
                    <input-button
                        icon="times"
                        v-on:clicked="$emit('close')"
                        data-e2e="close-execution-details-button"
                    />
                </template>
            </layout-row>
        </template>

        <template v-slot:body>
            <div>
                <h2>Time and state</h2>
                <div class="table execution-core">
                    <div class="tr">
                        <div class="td">State</div>
                        <div class="td align-left">
                            {{ jobExecution.executionState }}
                        </div>
                    </div>
                    <div class="tr" v-if="jobExecution.started == null">
                        <div class="td">Scheduled:</div>
                        <div class="td align-left">
                            {{ formatDate(jobExecution.created) }}
                        </div>
                    </div>
                    <div class="tr" v-if="jobExecution.started == null">
                        <div class="td">Waiting:</div>
                        <div class="td align-left">
                            {{ calculateDuration(jobExecution.created, jobExecution.finished) }}
                        </div>
                    </div>
                    <div class="tr" v-if="jobExecution.started != null">
                        <div class="td">Started:</div>
                        <div class="td align-left">
                            {{ formatDate(jobExecution.started) }}
                        </div>
                    </div>
                    <div class="tr" v-if="jobExecution.finished != null">
                        <div class="td">Finished:</div>
                        <div class="td align-left">
                            {{ formatDate(jobExecution.finished) }}
                        </div>
                    </div>
                    <div class="tr" v-if="jobExecution.started != null && jobExecution.executionState !== 'ACTIVE'">
                        <div class="td">Duration:</div>
                        <div class="td align-left">
                            {{ calculateDuration(jobExecution.started, jobExecution.finished) }}
                        </div>
                    </div>
                    <div class="tr" v-if="jobExecution.processedEvents > 0" data-e2e="job-execution-details-events">
                        <div class="td">Processed Events:</div>
                        <div class="td align-left">
                            {{ jobExecution.processedEvents }}
                        </div>
                    </div>
                </div>

                <div v-if="jobExecution.executionState === 'RUNNING'" class="max-width">
                    <h2 class="truncate" v-if="jobExecution.workInProgress.length > 0">Executing:</h2>

                    <feedback-box v-for="(wip, index) in jobExecution.workInProgress" :key="index" :clickable="false">
                        <template v-slot:left>
                            <div class="margin-right truncate">
                                {{ wip.name }}
                            </div>
                        </template>
                        <template v-slot:right>
                            <div>
                                {{ formatPercent(wip.progressInPercent) }}
                            </div>
                        </template>
                    </feedback-box>
                </div>

                <div v-if="jobExecution.errorCause">
                    <h2>Error cause</h2>
                    <pre><code>{{ jobExecution.errorCause }}
      </code>
    </pre>
                </div>
            </div>
        </template>
    </modal-dialog>
</template>

<script>
import ModalDialog from "../common/modal-dialog.vue";
import LayoutRow from "../common/layout-row.vue";
import InputButton from "../common/input-button.vue";
import FeedbackBox from "../common/feedback-box.vue";

export default {
    name: "job-execution-details",
    props: ["jobExecution"],
    components: { FeedbackBox, InputButton, LayoutRow, ModalDialog },
    methods: {
        formatDate: function (unformattedDate) {
            let options = { year: "numeric", month: "2-digit", day: "2-digit" };
            let date = new Date(unformattedDate);
            return date.toLocaleDateString(undefined, options) + " " + date.toLocaleTimeString();
        },
        formatPercent: function (input) {
            return input.toFixed(2) + "%";
        },
        calculateDuration: function (from, till) {
            let start = new Date(from).getTime();
            let end = Date.now();
            if (till != null) {
                end = new Date(till).getTime();
            }
            let delta = Math.floor((end - start) / 1000);
            let hours = Math.floor(delta / 3600) % 24;
            delta -= hours * 3600;
            let minutes = Math.floor(delta / 60) % 60;
            delta -= minutes * 60;
            let seconds = Math.floor(delta % 60);
            return (
                hours.toString().padStart(2, "0") +
                ":" +
                minutes.toString().padStart(2, "0") +
                ":" +
                seconds.toString().padStart(2, "0")
            );
        },
    },
};
</script>

<style scoped>
.execution-core {
    margin-bottom: 30px;
}

pre {
    padding: 10px;
    max-height: calc(100vh / 2);
    height: auto;
    overflow: auto;
    word-break: normal !important;
    word-wrap: normal !important;
    white-space: pre !important;
    background-color: var(--color-alert);
}

.margin-right {
    margin-right: 25px;
}
</style>
