<template>
    <div>
        <job-navigation-item
            class="job-navi-item"
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
            data-e2e="job-configuration"
        />

        <draggable :list="jobConfiguration.actions" item-key="id" handle=".move-icon" :group="'actions'">
            <template #item="{ element }">
                <action-navigation-item
                    :action="element"
                    :selected-action-id="selectedActionId"
                    :validation-errors="validationErrors"
                    :simulation-results="getActionSimulationResults(element.id)"
                    v-on:action-is-selected="$emit('action-is-selected', $event)"
                    v-on:delete-action="$emit('delete-action', $event)"
                />
            </template>
        </draggable>

        <layout-row class="add-action-row">
            <template v-slot:left>
                <label v-if="jobConfiguration.actions.length === 0">There are no actions defined yet.</label>
            </template>
            <template v-slot:right>
                <add-item-button
                    icon="plus"
                    label="Add new Action"
                    v-on:clicked="$emit('add-action')"
                    data-e2e="add-action-button"
                />
            </template>
        </layout-row>
    </div>
</template>

<script>
import draggable from "vuedraggable";
import JobNavigationItem from "./job-navigation-item.vue";
import AddItemButton from "./add-item-button.vue";
import LayoutRow from "../common/layout-row.vue";
import ActionNavigationItem from "./action-navigation-item.vue";

export default {
    name: "job-navigation",
    components: {
        ActionNavigationItem,
        LayoutRow,
        AddItemButton,
        JobNavigationItem,
        draggable,
    },
    props: [
        "jobConfiguration",
        "validationErrors",
        "selectedActionId",
        "jobRunningOrWaiting",
        "jobExecutionsPage",
        "simulationResults",
    ],
    methods: {
        getActionSimulationResults: function (actionId) {
            if (this.simulationResults && Object.prototype.hasOwnProperty.call(this.simulationResults, actionId)) {
                return this.simulationResults[actionId];
            }
            return null;
        },
    },
    computed: {
        jobSelected: function () {
            return this.selectedActionId === null;
        },
    },
};
</script>

<style scoped>
div {
    max-width: 25em;
}

.job-navi-item {
    padding: 0.5em;
}

.add-action-row {
    color: var(--color-background);
    padding: 0.25em 0 2em 0.25em;
}
</style>
