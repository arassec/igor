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
                             v-on:run-job="$emit('run-job')"/>

        <draggable v-model="jobConfiguration.tasks" group="tasks" @start="drag=true" @end="drag=false" handle=".move-icon">
            <task-navigation-item v-for="task of jobConfiguration.tasks" :key="task.id" :task="task"
                                  class="task-navi-item"
                                  :selected-task-id="selectedTaskId"
                                  :selected-action-id="selectedActionId"
                                  :validation-errors="validationErrors"
                                  v-on:delete-task="$emit('delete-task', $event)"
                                  v-on:duplicate-task="$emit('duplicate-task', $event)"
                                  v-on:add-action="$emit('add-action', $event)"
                                  v-on:delete-action="$emit('delete-action', $event)"
                                  v-on:task-is-selected="$emit('task-is-selected', $event)"
                                  v-on:action-is-selected="$emit('action-is-selected', $event)"/>
        </draggable>

        <layout-row class="add-task-row">
            <label slot="left" v-if="jobConfiguration.tasks.length === 0">There are no tasks defined yet.</label>
            <add-item-button slot="right" icon="plus" label="Add new Task" v-on:clicked="$emit('add-task')" class="add-task-button"/>
        </layout-row>

    </div>
</template>

<script>
    import draggable from 'vuedraggable'
    import JobNavigationItem from "./job-navigation-item";
    import TaskNavigationItem from "./task-navigation-item";
    import AddItemButton from "./add-item-button";
    import LayoutRow from "../common/layout-row";

    export default {
        name: "job-navigation",
        components: {LayoutRow, AddItemButton, TaskNavigationItem, JobNavigationItem, draggable},
        props: ['jobConfiguration', 'validationErrors', 'selectedTaskId', 'selectedActionId', 'jobRunningOrWaiting', 'jobExecutionsPage'],
        computed: {
            jobSelected: function () {
                return (this.selectedTaskId === null && this.selectedActionId === null);
            }
        }
    }
</script>

<style scoped>

    div {
        max-width: 25em;
    }

    .job-navi-item {
        margin: 0 0 1em 0;
        padding: .5em;
    }

    .task-navi-item {
        margin: .5em 0 0 0;
    }

    .task-navi-item:active {
        cursor: move;
    }

    .add-task-row {
        color: var(--color-background);
        margin: .5em 0 1em .25em;
    }

</style>