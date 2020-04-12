<template>
    <div>
        <navigation-item v-on:clicked="$emit('task-is-selected', task.id)" :selected="taskSelected"
                         :has-validation-errors="validationErrors.includes(task.id)">
            <font-awesome-icon slot="left" icon="grip-vertical" class="margin-right fa-fw move-icon"/>
            <label slot="center" class="task-label">
                <font-awesome-icon icon="bolt" class="fa-fw sub-validatin-failed" v-if="subValidationFailed"/>
                {{ task.name.length > 0 ? task.name : 'Task' }}</label>
            <icon-button slot="right" icon="ellipsis-v" v-on:clicked="showMenu = !showMenu"/>

            <div slot="menu" class="menu" v-show="showMenu" v-on:mouseleave="showMenu = false">
                <ul>
                    <li class="menu-item" v-on:click.stop="$emit('duplicate-task', task.id)">
                        <font-awesome-icon icon="clone" class="margin-right fa-fw"/>
                        Duplicate
                    </li>
                    <li class="menu-item" v-on:click.stop="$emit('delete-task', task.id)">
                        <font-awesome-icon icon="trash" class="margin-right fa-fw"/>
                        Delete
                    </li>
                </ul>
            </div>
        </navigation-item>

        <transition name="fade">
            <div v-show="expanded" style="display: flex; flex-direction: column;">
                <draggable v-model="task.actions" :group="task.id + '_actions'" @start="drag=true" @end="drag=false"
                           handle=".move-icon">
                    <action-navigation-item v-for="action of task.actions" :key="action.id" :action="action"
                                            :selected-action-id="selectedActionId"
                                            :validation-errors="validationErrors"
                                            v-on:action-is-selected="$emit('action-is-selected', $event)"
                                            v-on:delete-action="$emit('delete-action', $event)"/>
                </draggable>

                <layout-row class="add-action-row">
                    <label slot="left" v-if="task.actions.length === 0">There are no actions defined
                        yet.</label>
                    <add-item-button slot="right" icon="plus" label="Add new Action"
                                     v-on:clicked="$emit('add-action', task.id)"/>
                </layout-row>
            </div>
        </transition>
    </div>
</template>

<script>
    import draggable from 'vuedraggable'
    import Utils from '../../utils/utils.js'
    import ActionNavigationItem from "./action-navigation-item";
    import AddItemButton from "./add-item-button";
    import NavigationItem from "./navigation-item";
    import IconButton from "../common/icon-button";
    import LayoutRow from "../common/layout-row";

    export default {
        name: "task-navigation-item",
        components: {LayoutRow, IconButton, NavigationItem, AddItemButton, ActionNavigationItem, draggable},
        props: ['task', 'selectedTaskId', 'selectedActionId', 'validationErrors'],
        data: function () {
            return {
                showMenu: false
            }
        },
        computed: {
            expanded: function () {
                let actionIndex = Utils.findActionIndex(this.task, this.selectedActionId);
                return (this.selectedTaskId === this.task.id) || (actionIndex !== -1);
            },
            taskSelected: function () {
                return (this.selectedTaskId === this.task.id);
            },
            subValidationFailed: function () {
                let validationFailed = false;
                this.task.actions.forEach((action) => {
                    if (this.validationErrors.includes(action.id)) {
                        validationFailed = true;
                    }
                });
                return validationFailed;
            }
        }
    }
</script>

<style scoped>

    .sub-validatin-failed {
        color: var(--color-alert);
    }

    .task-label {
        display: inline-block;
        padding: .2em 0 .2em 0;
    }

    .add-action-row {
        color: var(--color-background);
        margin: .25em 0 .5em .25em;
    }

    .fade-enter-active {
        transition: opacity 0.5s ease;
    }

    .fade-enter, .fade-leave-to {
        opacity: 0;
    }

</style>