<template>
    <div class="treeview">
        <ul>
            <li>
                <span class="item truncate"
                     v-bind:class="getJobStyleClass()"
                     v-on:click="$emit('job-is-selected')">
                    <font-awesome-icon icon="toolbox"/>
                    <span>
                                {{jobConfiguration.name.length > 0 ? jobConfiguration.name : 'Unnamed Job'}}
                            </span>
                </span>
                <ul class="tree">
                    <li v-for="(task, taskIndex) in jobConfiguration.tasks"
                        v-bind:key="taskIndex">
                        <span class="item truncate"
                             v-bind:class="getTaskStyleClass(taskIndex, task)"
                             v-on:click="$emit('task-is-selected', taskIndex)">
                            <font-awesome-icon icon="tasks"/>
                            <span>
                                        {{task.name.length > 0 ? task.name : 'Unnamed Task'}}
                                    </span>
                            <font-awesome-icon icon="arrow-up" class="fa-xs"
                                               v-if="taskIndex > 0"
                                               v-on:click.stop="$emit('move-task-up', taskIndex)"/>
                            <font-awesome-icon icon="arrow-down" class="button-margin-left fa-xs"
                                               v-if="taskIndex < jobConfiguration.tasks.length -1"
                                               v-on:click.stop="$emit('move-task-down', taskIndex)"/>
                            <font-awesome-icon icon="clone" class="button-margin-left fa-xs"
                                               v-on:click.stop="$emit('duplicate-task', taskIndex)"/>
                            <font-awesome-icon icon="trash-alt" class="button-margin-left fa-xs"
                                               v-on:click.stop="$emit('delete-task', taskIndex)"/>
                        </span>
                        <ul>
                            <li v-for="(action, actionIndex) in task.actions"
                                v-bind:key="actionIndex">
                                <span class="item truncate"
                                     v-bind:class="getActionStyleClass(taskIndex, actionIndex, task, action)"
                                     v-on:click="$emit('action-is-selected', taskIndex, actionIndex)">
                                    <font-awesome-icon icon="wrench"/>
                                    <span>
                                                {{ action.type.label}}
                                            </span>
                                    <font-awesome-icon icon="arrow-up" class="fa-xs"
                                                       v-if="actionIndex > 0"
                                                       v-on:click.stop="$emit('move-action-up', taskIndex, actionIndex)"/>
                                    <font-awesome-icon icon="arrow-down" class="button-margin-left fa-xs"
                                                       v-if="actionIndex < jobConfiguration.tasks[taskIndex].actions.length - 1"
                                                       v-on:click.stop="$emit('move-action-down', taskIndex, actionIndex)"/>
                                    <font-awesome-icon icon="trash-alt" class="button-margin-left fa-xs"
                                                       v-on:click.stop="$emit('delete-action', taskIndex, actionIndex)"/>
                                </span>
                            </li>
                            <li>
                                <span class="item truncate" :class="getAddActionStyleClass(task)"
                                     v-on:click="$emit('add-action', taskIndex)">
                                    <font-awesome-icon :icon="['fas', 'plus-circle']" class="font-18"/>
                                    <span>
                                                Add Action
                                            </span>
                                </span>
                            </li>
                        </ul>
                    </li>
                    <li>
                        <div class="item truncate" :class="getAddTaskStyleClass()" v-on:click="$emit('add-task')">
                            <font-awesome-icon :icon="['fas', 'plus-circle']"/>
                            <span>
                                        Add Task
                                    </span>
                        </div>
                    </li>
                </ul>
            </li>
        </ul>
    </div>
</template>

<script>

    export default {
        name: 'job-tree-navigation',
        props: ['jobConfiguration', 'validationErrors', 'selectedTaskIndex', 'selectedActionIndex'],
        methods: {
            isTaskSelected: function (index) {
                return (index == this.selectedTaskIndex && this.selectedActionIndex == -1)
            },
            getJobStyleClass: function () {
                if (this.hasValidationErrors(-1, -1)) {
                    return 'validation-error'
                }
                if (this.selectedTaskIndex == -1 && this.selectedActionIndex == -1) {
                    return 'selected'
                }
                if (!this.jobConfiguration.active) {
                    return 'inactive'
                }
                return ''
            },
            getTaskStyleClass: function (taskIndex, task) {
                if (this.hasValidationErrors(taskIndex, -1)) {
                    return 'validation-error'
                }
                if (this.selectedTaskIndex == taskIndex && this.selectedActionIndex == -1) {
                    return 'selected'
                }
                if (!this.jobConfiguration.active || !task.active) {
                    return 'inactive'
                }
                return ''
            },
            getActionStyleClass: function (taskIndex, actionIndex, task, action) {
                if (this.validationErrors.indexOf(taskIndex + '_' + actionIndex) > -1) {
                    return 'validation-error'
                }
                if (taskIndex == this.selectedTaskIndex && actionIndex == this.selectedActionIndex) {
                    return 'selected'
                }
                if (!this.jobConfiguration.active || !task.active) {
                    return 'inactive'
                }
                if (action.parameters) {
                    for (let i in action.parameters) {
                        if (action.parameters[i].name === 'active' && action.parameters[i].value === false) {
                            return 'inactive'
                        }
                    }
                }
                return ''
            },
            getAddTaskStyleClass: function () {
                if (!this.jobConfiguration.active) {
                    return 'inactive'
                }
            },
            getAddActionStyleClass: function (task) {
                if (!this.jobConfiguration.active) {
                    return 'inactive'
                }
                if (!task.active) {
                    return 'inactive'
                }
            },
            isActionSelected: function (taskIndex, actionIndex) {
                return taskIndex == this.selectedTaskIndex && actionIndex == this.selectedActionIndex
            },
            hasValidationErrors: function (taskIndex, actionIndex) {
                return (this.validationErrors.indexOf(taskIndex + '_' + actionIndex) > -1)
            }
        }
    }
</script>

<style scoped>

    .fa-xs:hover {
        height: 15px;
        width: 15px;
    }

    .treeview {
        color: var(--font-color-light);
        overflow: hidden;
    }

    .item {
        max-width: 300px;
        display: inline-block;
        padding: 4px 4px 0px 5px;
    }

    .item:hover {
        cursor: pointer;
        font-weight: bold;
    }

    .selected {
        font-weight: bold;
        background-color: var(--info-background-color);
    }

    .validation-error {
        background-color: var(--alert-background-color) !important;
    }

    .item-icon {
        display: inline-block;
        border-radius: 50%;

        width: 18px;
        height: 18px;

        background: transparent;
        border: 1px solid var(--font-color-light);
        color: var(--font-color-light);

        text-align: center;
        line-height: 16px;
    }

    ul.tree, ul.tree ul {
        list-style: none;
        margin-left: 7px;
        padding: 0;
    }

    ul.tree ul {
        margin-left: 18px;
    }

    ul.tree li {
        margin: 0;
        padding: 0 7px;
        color: var(--font-color-light);
        border-left: 1px solid var(--font-color-light);
    }

    ul.tree li:last-child {
        border-left: none;
    }

    ul.tree li:before {
        position: relative;
        top: -0.6em;
        height: 1em;
        width: 12px;
        color: white;
        border-bottom: 1px solid var(--font-color-light);
        content: "";
        display: inline-block;
        left: -7px;
    }

    ul.tree li:last-child:before {
        border-left: 1px solid var(--font-color-light);
    }


</style>
