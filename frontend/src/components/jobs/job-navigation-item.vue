<template>
    <div :class="style" v-on:click="$emit('job-is-selected')">
        <h1>
            <font-awesome-icon icon="toolbox" class="margin-right fa-fw"/>
            Job Configuration
        </h1>
        <layout-row>
            <input-button slot="left" icon="chevron-left" v-on:clicked="$emit('show-executions')"
                          :class="jobExecutionFailed ? 'alert' : ''"/>
            <input-button slot="left" icon="save" v-on:clicked="$emit('save-configuration')" class="margin-left"/>
            <input-button slot="right" icon="plug" v-on:clicked="$emit('test-configuration')"/>
            <input-button slot="right" :icon="playIcon" v-on:clicked="$emit('run-job')" class="margin-left"
                          :disabled="jobRunningOrWaiting || job.id == null || !job.active || disableDueToFaultIntolerance"/>
        </layout-row>
    </div>
</template>

<script>
import InputButton from "../common/input-button";
import LayoutRow from "../common/layout-row";

export default {
    name: "job-navigation-item",
    components: {LayoutRow, InputButton},
    props: ['job', 'selected', 'validationErrors', 'jobRunningOrWaiting', 'jobExecutionsPage'],
    computed: {
        disableDueToFaultIntolerance: function () {
            if (this.jobExecutionsPage.items.length > 0) {
                let state = this.jobExecutionsPage.items[0].state;
                if (state === 'FAILED' && !this.job.faultTolerant) {
                    return true;
                }
            }
            return false;
        },
        style: function () {
            if (this.job.id in this.validationErrors) {
                return "alert";
            }
            if (this.job.trigger.id in this.validationErrors) {
                return "alert";
            }
            if (this.selected) {
                return "info";
            }
            if (!this.job.active) {
                return "inactive";
            }
            return "unselected";
        },
        jobExecutionFailed: function () {
            if (this.jobExecutionsPage) {
                for (let i = 0; i < this.jobExecutionsPage.items.length; i++) {
                    if ('FAILED' === this.jobExecutionsPage.items[i].state) {
                        return true
                    }
                }
            }
            return false
        },
        playIcon: function () {
            if (this.disableDueToFaultIntolerance) {
                return 'bolt';
            } else if (this.jobExecutionsPage.items.length > 0) {
                let state = this.jobExecutionsPage.items[0].state;
                if (state === 'WAITING') {
                    return 'hourglass';
                } else if (state === 'RUNNING') {
                    return 'circle-notch';
                } else if (state === 'ACTIVE') {
                    return 'sign-in-alt';
                }
            }
            return 'play';
        }
    }
}
</script>

<style scoped>

div:hover {
    cursor: pointer;
}

.unselected {
    background-color: var(--color-background);
}

.unselected:hover {
    background-color: var(--color-foreground);
}

.inactive {
    background-color: var(--color-background);
    opacity: 0.75;
}

.alert {
    background-color: var(--color-alert);
}
</style>