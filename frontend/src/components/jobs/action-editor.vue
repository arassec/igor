<template>
    <core-panel class="action-editor">

        <test-result-marker v-if="showTestResultMarker()" v-on:clicked="$emit('show-action-test-results')"/>

        <h1>
            <font-awesome-icon icon="wrench"/>
            {{ action.label }}
        </h1>

        <table>
            <tr>
                <td>Type</td>
                <td>
                    <select v-model="action.type" v-on:change="loadTypeParameters(action)">
                        <option v-for="actionType in actionTypes" v-bind:value="actionType.type"
                                v-bind:key="actionType.type">
                            {{actionType.label}}
                        </option>
                    </select>
                </td>
            </tr>
        </table>

        <div class="action-parameters">
            <h3>Parameters</h3>
            <parameter-editor :parameters="action.parameters" ref="parameterEditor"/>
        </div>

        <button-row>
            <p slot="right">
                <input-button icon="arrow-up" v-if="actionIndex > 0" v-on:clicked="$emit('move-up')"
                              class="button-margin-right"/>
                <input-button icon="arrow-down" v-if="actionIndex < numActions -1" v-on:clicked="$emit('move-down')"
                              class="button-margin-right"/>
                <input-button icon="trash-alt" v-on:clicked="$emit('delete-action')"/>
            </p>
        </button-row>

    </core-panel>
</template>

<script>
import ParameterEditor from '../common/parameter-editor'
import CorePanel from '../common/core-panel'
import TestResultMarker from './test-result-marker'
import ButtonRow from '../common/button-row'
import InputButton from '../common/input-button'

export default {
  name: 'action-editor',
  components: {InputButton, ButtonRow, TestResultMarker, CorePanel, ParameterEditor},
  props: ['action', 'actionIndex', 'taskIndex', 'testResults', 'numActions'],
  data: function () {
    return {
      actionTypes: []
    }
  },
  methods: {
    loadActionTypes: function () {
      let component = this
      this.$http.get('/api/actiontype').then(function (response) {
        for (let i = component.actionTypes.length; i > 0; i--) {
          component.actionTypes.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.actionTypes.push(item)
        })
        if (typeof component.action.type === 'undefined' || component.action.type === '') {
          component.action.type = component.actionTypes[0].type
          component.action.label = component.actionTypes[0].label
          component.loadTypeParameters(component.action)
        }
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    loadTypeParameters: function (action) {
      for (let i = 0; i < this.actionTypes.length; i++) {
        if (this.actionTypes[i].type === action.type) {
          this.action.label = this.actionTypes[i].label
        }
      }
      let component = this
      this.$http.get('/api/actionparams/' + action.type).then(function (response) {
        component.action.parameters = response.data
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    validateInput: function () {
      let parameterValidationResult = true
      if (typeof this.$refs.parameterEditor !== 'undefined') {
        parameterValidationResult = this.$refs.parameterEditor.validateInput()
      }
      this.$forceUpdate()
      return parameterValidationResult
    },
    showTestResultMarker: function () {
      if (this.testResults != null && this.testResults.taskResults != null) {
        let taskResults = this.testResults.taskResults
        if (taskResults[this.taskIndex] != null && taskResults[this.taskIndex].actionResults != null) {
          let actionResults = taskResults[this.taskIndex].actionResults
          if (actionResults[this.actionIndex] != null) {
            return true
          }
        }
      }
      return false
    }
  },
  mounted () {
    this.loadActionTypes()
  }
}
</script>

<style scoped>

    .action-parameters {
        margin-top: 25px;
    }

    table tr td h1 {
        margin-bottom: 0px;
    }
</style>
