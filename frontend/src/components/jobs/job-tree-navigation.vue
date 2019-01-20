<template>
    <div class="treeview">
        <ul>
            <li>
                        <span class="item"
                              v-bind:class="{ 'selected': selectedTaskIndex == -1 && selectedActionIndex == -1, 'validation-error': hasValidationErrors(-1, -1) }"
                              v-on:click="$emit('job-is-selected')">
                            <font-awesome-icon icon="toolbox"/>
                            <span>
                                {{jobConfiguration.name.length > 0 ? formatName(jobConfiguration.name) : 'Unnamed Job'}}
                            </span>
                        </span>
                <ul class="tree">
                    <li v-for="(task, taskIndex) in jobConfiguration.tasks"
                        v-bind:key="taskIndex">
                                <span class="item"
                                      v-bind:class="{ 'selected': isTaskSelected(taskIndex), 'validation-error': hasValidationErrors(taskIndex, -1)}"
                                      v-on:click="$emit('task-is-selected', taskIndex)">
                                    <font-awesome-icon icon="tasks"/>
                                    <span>
                                        {{task.name.length > 0 ? formatName(task.name) : 'Unnamed Task'}}
                                    </span>
                                    <font-awesome-icon icon="arrow-up" class="fa-xs"
                                                       v-if="taskIndex > 0"
                                                       v-on:click.stop="$emit('move-task-up', taskIndex)"/>
                                    <font-awesome-icon icon="arrow-down" class="button-margin-left fa-xs"
                                                       v-if="taskIndex < jobConfiguration.tasks.length -1"
                                                       v-on:click.stop="$emit('move-task-down', taskIndex)"/>
                                    <font-awesome-icon icon="trash-alt" class="button-margin-left fa-xs"
                                                       v-on:click="$emit('delete-task', taskIndex)"/>
                                </span>
                        <ul>
                            <li v-for="(action, actionIndex) in task.actions"
                                v-bind:key="actionIndex">
                                        <span class="item"
                                              v-bind:class="{ 'selected': isActionSelected(taskIndex, actionIndex), 'validation-error': hasValidationErrors(taskIndex, actionIndex)}"
                                              v-on:click="$emit('action-is-selected', taskIndex, actionIndex)">
                                            <font-awesome-icon icon="wrench"/>
                                            <span>
                                                {{ formatName(action.type.label)}}
                                            </span>
                                            <font-awesome-icon icon="arrow-up" class="fa-xs"
                                                               v-if="actionIndex > 0"
                                                               v-on:click.stop="$emit('move-action-up', taskIndex, actionIndex)"/>
                                            <font-awesome-icon icon="arrow-down" class="button-margin-left fa-xs"
                                                               v-if="actionIndex < jobConfiguration.tasks[taskIndex].actions.length - 1"
                                                               v-on:click.stop="$emit('move-action-down', taskIndex, actionIndex)"/>
                                            <font-awesome-icon icon="trash-alt" class="button-margin-left fa-xs"
                                                               v-on:click="$emit('delete-action', taskIndex, actionIndex)"/>
                                        </span>
                            </li>
                            <li>
                                        <span class="item" v-on:click="$emit('add-action', taskIndex)">
                                            <font-awesome-icon :icon="['fas', 'plus-circle']" class="font-18"/>
                                            <span>
                                                Add Action
                                            </span>
                                        </span>
                            </li>
                        </ul>
                    </li>
                    <li>
                                <span class="item" v-on:click="$emit('add-task')">
                                    <font-awesome-icon :icon="['fas', 'plus-circle']"/>
                                    <span>
                                        Add Task
                                    </span>
                                </span>
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
    isActionSelected: function (taskIndex, actionIndex) {
      return taskIndex == this.selectedTaskIndex && actionIndex == this.selectedActionIndex
    },
    hasValidationErrors: function (taskIndex, actionIndex) {
      return (this.validationErrors.indexOf(taskIndex + '_' + actionIndex) > -1)
    },
    formatName: function (name) {
      if (name.length > 30) {
        return name.substring(0, 30) + '...'
      }
      return name
    }
  }
}
</script>

<style scoped>

    .treeview {
        color: var(--font-color-light);
        line-height: 25px;
    }

    .item {
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
        line-height: 25px;
        color: var(--font-color-light);
        border-left: 1px solid var(--font-color-light);
    }

    ul.tree li:last-child {
        border-left: none;
    }

    ul.tree li:before {
        position: relative;
        top: -0.3em;
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
