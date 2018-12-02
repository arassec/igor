<template>
    <div class="container">
        <core-panel>
            <h1>Job Configuration</h1>

            <slot name="feedback"/>

            <div class="job-buttons">
                <button-row>
                    <p slot="left">
                        <input-button icon="plug" v-on:clicked="$emit('test-job')" class="button-margin-right"/>
                        <input-button icon="save" v-on:clicked="$emit('save-job')"/>
                    </p>
                    <p slot="right">
                        <input-button icon="times" v-on:clicked="$emit('cancel-job')" disabled="true"/>
                        <input-button icon="play" v-on:clicked="$emit('run-job')" class="button-margin-left"/>
                    </p>
                </button-row>
            </div>

            <div class="treeview">
                <ul>
                    <li>
                        <span class="item" v-bind:class="{ 'selected': jobSelected, 'validation-error': jobValidation.hasErrors }"
                              v-on:click="$emit('job-is-selected')">
                            <font-awesome-icon icon="toolbox"/>
                            <span>
                                {{jobConfiguration.name}}
                            </span>
                        </span>
                        <ul class="tree">
                            <li v-for="(task, taskIndex) in jobConfiguration.tasks"
                                v-bind:key="taskIndex">
                                <span class="item" v-bind:class="{ 'selected': isTaskSelected(taskIndex)}"
                                      v-on:click="$emit('task-is-selected', taskIndex)">
                                    <font-awesome-icon icon="tasks"/>
                                    <!--
                                    <font-awesome-layers>
                                      <font-awesome-icon :icon="['far', 'circle']" transform="grow-4" />
                                      <font-awesome-icon icon="tasks" transform="shrink-2" />
                                    </font-awesome-layers>
                                    -->
                                    <span>
                                        {{task.name}}
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
                                              v-bind:class="{ 'selected': isActionSelected(taskIndex, actionIndex)}"
                                              v-on:click="$emit('action-is-selected', taskIndex, actionIndex)">
                                        <font-awesome-icon icon="wrench"/>
                                            <span :class="isActionSelected(taskIndex, actionIndex) ? 'bold item' : 'item'">
                                                {{action.label}}
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
        </core-panel>
    </div>
</template>

<script>
import CorePanel from '../common/core-panel'
import ButtonRow from '../common/button-row'
import InputButton from '../common/input-button'

export default {
  name: 'job-tree-navigation',
  components: {InputButton, ButtonRow, CorePanel},
  props: ['jobConfiguration', 'jobValidation', 'jobSelected', 'selectedTaskIndex', 'selectedActionIndex'],
  methods: {
    isTaskSelected: function (index) {
      return (index == this.selectedTaskIndex && this.selectedActionIndex == -1)
    },
    isActionSelected: function (taskIndex, actionIndex) {
      return taskIndex == this.selectedTaskIndex && actionIndex == this.selectedActionIndex
    }
  }
}
</script>

<style scoped>

    .container {
        flex-grow: 1;
        flex-basis: 0;
    }

    .job-buttons {
        padding-bottom: 10px;
        border-bottom: 1px solid var(--font-color-light);
        margin-bottom: 15px;
    }

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
