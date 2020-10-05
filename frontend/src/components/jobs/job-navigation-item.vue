<template>
    <div :class="style" v-on:click="$emit('job-is-selected')">
        <h1>
            <font-awesome-icon icon="toolbox" class="margin-right fa-fw"/>
            {{ job.name.length > 0 ? job.name : 'Job' }}
        </h1>
        <layout-row>
            <input-button slot="left" icon="arrow-left" v-on:clicked="$emit('cancel-configuration')"
                          class="margin-right"/>
            <input-button slot="left" icon="cogs" v-on:clicked="$emit('test-configuration')" class="margin-right"/>
            <input-button slot="left" icon="save" v-on:clicked="$emit('save-configuration')"/>
            <input-button slot="right" icon="play" v-on:clicked="$emit('run-job')" class="margin-left"
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

</style>