<template>
    <div>
        <job-navigation-item class="job-navi-item"
                             :job="jobConfiguration"
                             :selected="jobSelected"
                             :validation-errors="validationErrors"
                             :job-running-or-waiting="jobRunningOrWaiting"
                             :job-executions-page="jobExecutionsPage"
                             v-on:job-is-selected="$emit('job-is-selected')"
                             v-on:cancel-configuration="$emit('cancel-configuration')"
                             v-on:test-configuration="$emit('test-configuration')"
                             v-on:save-configuration="$emit('save-configuration')"
                             v-on:show-executions="$emit('show-executions')"
                             v-on:run-job="$emit('run-job')"
                             data-e2e="job-configuration"/>

        <draggable v-model="jobConfiguration.actions" :group="'actions'" @start="drag=true" @end="drag=false"
                   handle=".move-icon">
            <action-navigation-item v-for="action of jobConfiguration.actions" :key="action.id" :action="action"
                                    :selected-action-id="selectedActionId"
                                    :validation-errors="validationErrors"
                                    :simulation-results="getActionSimulationResults(action.id)"
                                    v-on:action-is-selected="$emit('action-is-selected', $event)"
                                    v-on:delete-action="$emit('delete-action', $event)"/>
        </draggable>

        <layout-row class="add-action-row">
            <label slot="left" v-if="jobConfiguration.actions.length === 0">There are no actions defined
                yet.</label>
            <add-item-button slot="right" icon="plus" label="Add new Action"
                             v-on:clicked="$emit('add-action')"
                             data-e2e="add-action-button"/>
        </layout-row>

    </div>
</template>

<script>
import draggable from 'vuedraggable'
import JobNavigationItem from "./job-navigation-item";
import AddItemButton from "./add-item-button";
import LayoutRow from "../common/layout-row";
import ActionNavigationItem from "./action-navigation-item";

export default {
    name: "job-navigation",
    components: {ActionNavigationItem, LayoutRow, AddItemButton, JobNavigationItem, draggable},
    props: ['jobConfiguration', 'validationErrors', 'selectedActionId', 'jobRunningOrWaiting', 'jobExecutionsPage', 'simulationResults'],
    methods: {
        getActionSimulationResults: function(actionId) {
            if (this.simulationResults && this.simulationResults.hasOwnProperty(actionId)) {
                return this.simulationResults[actionId];
            }
            return null;
        }
    },
    computed: {
        jobSelected: function () {
            return (this.selectedActionId === null);
        }
    }
}
</script>

<style scoped>

div {
    max-width: 25em;
}

.job-navi-item {
    padding: .5em;
}

.add-action-row {
    color: var(--color-background);
    padding: .25em 0 2em .25em;
}

</style>