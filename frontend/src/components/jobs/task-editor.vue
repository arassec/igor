<template>
  <div class="task-editor">
    <core-panel>

      <test-result-marker v-if="showTestResultMarker()" v-on:clicked="$emit('show-task-test-results')"/>

      <h1><font-awesome-icon icon="tasks"/> {{ task.name.length > 0 ? task.name : 'New Task' }}</h1>

      <table>
        <tr>
          <td><label>Name</label></td>
          <td>
            <input type="text" autocomplete="off" v-model="task.name"/>
          </td>
          <td>
            <validation-error v-if="nameValidationError.length > 0">
              {{nameValidationError}}
            </validation-error>
          </td>
        </tr>
        <tr>
          <td><label>Description</label></td>
          <td>
            <input type="text" autocomplete="off" v-model="task.description"/>
          </td>
          <td/>
        </tr>
        <tr>
          <td><label>Provider</label></td>
          <td>
            <select v-model="task.provider.type" v-on:change="loadTypeParameters(task.provider.type)">
              <option v-for="providerType in providerTypes" v-bind:value="providerType.type"
                      v-bind:key="providerType.type">
                {{providerType.label}}
              </option>
            </select>
          </td>
        </tr>
      </table>

      <div class="provider-parameters">
        <h3>Parameters</h3>
        <parameter-editor :parameters="task.provider.parameters" ref="parameterEditor"/>
      </div>

      <button-row>
        <input-button slot="right" v-on:clicked="showDeleteTaskDialog = true" icon="trash-alt"/>
      </button-row>

      <modal-dialog v-if="showDeleteActionDialog" @close="showDeleteActionDialog = false">
        <p slot="header">Delete Action?</p>
        <p slot="body">Do you really want to delete this Action?</p>
        <div slot="footer">
          <button-row>
            <input-button slot="left" v-on:clicked="showDeleteActionDialog = false" icon="times"/>
            <input-button slot="right" v-on:clicked="deleteAction()" icon="check"/>
          </button-row>
        </div>
      </modal-dialog>

    </core-panel>

    <action-editor v-for="(action, index) in task.actions"
                   v-bind:action="action"
                   v-bind:index="index"
                   v-bind:key="index"
                   v-bind:task-index="taskIndex"
                   v-bind:action-index="index"
                   v-bind:test-results="testResults"
                   v-bind:num-actions="task.actions.length"
                   v-on:show-action-test-results="showActionTestResult(index)"
                   v-on:move-up="moveActionUp(index)"
                   v-on:move-down="moveActionDown(index)"
                   v-on:delete-action="prepareDeleteAction(index)"
                   ref="actionEditors"/>

  </div>
</template>

<script>
import CorePanel from '../common/core-panel'
import InputButton from '../common/input-button'
import ParameterEditor from '../common/parameter-editor'
import ButtonRow from '../common/button-row'
import ValidationError from '../common/validation-error'
import ModalDialog from '../common/modal-dialog'
import TestResultMarker from './test-result-marker'
import ActionEditor from './action-editor'

export default {
  name: 'task-editor',
  components: {
    ActionEditor,
    TestResultMarker,
    ModalDialog,
    ValidationError,
    ButtonRow,
    ParameterEditor,
    CorePanel,
    InputButton
  },
  props: ['task', 'taskIndex', 'testResults'],
  data: function () {
    return {
      feedback: '',
      feedbackOk: true,
      showDeleteActionDialog: false,
      deleteActionIndex: 0,
      nameValidationError: '',
      requestInProgress: false,
      providerTypes: []
    }
  },
  methods: {
    loadProviderTypes: function () {
      let component = this
      this.$http.get('/api/providertype').then(function (response) {
        for (let i = component.providerTypes.length; i > 0; i--) {
          component.providerTypes.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.providerTypes.push(item)
        })
        if (typeof component.task.provider.type === 'undefined' || component.task.provider.type === '') {
          component.task.provider.type = component.providerTypes[0].type
          component.loadTypeParameters(component.task.provider.type)
        }
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    loadTypeParameters: function (type) {
      let component = this
      this.$http.get('/api/providerparams/' + type).then(function (response) {
        component.task.provider.parameters = response.data
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    validateInput: function () {
      this.nameValidationError = ''

      let nameValidationResult = true
      if (this.task.name == null || this.task.name === '') {
        this.nameValidationError = 'Name must be set'
        nameValidationResult = false
      }

      let parameterValidationResult = true
      if (typeof this.$refs.parameterEditor !== 'undefined') {
        parameterValidationResult = this.$refs.parameterEditor.validateInput()
      }

      let actionEditorsResult = true
      for (let i in this.$refs.actionEditors) {
        actionEditorsResult = (this.$refs.actionEditors[i].validateInput() && actionEditorsResult)
      }

      this.$forceUpdate()

      return (nameValidationResult && parameterValidationResult && actionEditorsResult)
    },
    deleteTask: function () {
      this.showDeleteTaskDialog = false
      this.$emit('delete')
    },
    addAction: function () {
      let action = {
        type: '',
        parameters: {}
      }
      this.task.actions.push(action)
    },
    prepareDeleteAction: function (index) {
      this.deleteActionIndex = index
      this.showDeleteActionDialog = true
    },
    deleteAction: function () {
      this.showDeleteActionDialog = false
      this.task.actions.splice(this.deleteActionIndex, 1)
    },
    showTestResultMarker: function () {
      if (this.testResults != null && this.testResults.taskResults != null) {
        let taskResults = this.testResults.taskResults
        if (taskResults[this.taskIndex] != null && taskResults[this.taskIndex].actionResults != null) {
          return true
        }
      }
      return false
    },
    showActionTestResult: function (index) {
      this.$emit('show-action-test-results', this.taskIndex, index)
    },
    moveActionUp: function (index) {
      if (index === 0) {
        return
      }
      this.arrayMove(this.task.actions, index, index - 1)
    },
    moveActionDown: function (index) {
      if (index < this.task.actions.length - 1) {
        this.arrayMove(this.task.actions, index, index + 1)
      }
    },
    arrayMove: function (array, oldIndex, newIndex) {
      if (newIndex >= array.length) {
        let k = newIndex - array.length + 1
        while (k--) {
          array.push(undefined)
        }
      }
      array.splice(newIndex, 0, array.splice(oldIndex, 1)[0])
    }
  },
  mounted () {
    this.loadProviderTypes()
  }
}
</script>

<style scoped>

  .task-editor {
    margin-top: 35px;
  }

  .provider-parameters {
    margin: 25px 0px 0px 0px;
  }

  .add-action-button {
    color: var(--font-color-light);
    background-color: var(--panel-background-color);
  }

</style>
