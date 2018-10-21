<template>
  <div class="content">
    <core-panel>
      <h1>Task{{ task.name != null ? ': ' + task.name : ''}}</h1>
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
        <h3>Provider Parameters</h3>
        <parameter-editor :parameters="task.provider.parameters" ref="parameterEditor"/>
      </div>

      <button-row>
        <p slot="right">
          <input-button v-on:clicked="showDeleteDialog = true" icon="trash-alt"/>
          <input-button v-on:clicked="addAction()" icon="plus"/>
        </p>
      </button-row>

      <modal-dialog v-if="showDeleteDialog" @close="showDeleteDialog = false">
        <p slot="header">Delete Task?</p>
        <p slot="body" v-if="task.name.length > 0">Do you really want to delete task '{{task.name}}'?</p>
        <p slot="body" v-if="task.name.length == 0">Do you really want to delete this task?</p>
        <div slot="footer">
          <button-row>
            <input-button slot="left" v-on:clicked="showDeleteDialog = false" icon="times"/>
            <input-button slot="right" v-on:clicked="deleteTask()" icon="check"/>
          </button-row>
        </div>
      </modal-dialog>

    </core-panel>
  </div>
</template>

<script>
import CorePanel from '../common/core-panel'
import InputButton from '../common/input-button'
import ParameterEditor from '../common/parameter-editor'
import ButtonRow from '../common/button-row'
import ValidationError from '../common/validation-error'
import ModalDialog from '../common/modal-dialog'

export default {
  name: 'task-editor',
  components: {ModalDialog, ValidationError, ButtonRow, ParameterEditor, CorePanel, InputButton},
  props: ['task'],
  data: function () {
    return {
      feedback: '',
      feedbackOk: true,
      showDeleteDialog: false,
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
        component.task.provider.type = component.providerTypes[0].type
        component.loadTypeParameters(component.task.provider.type)
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

      return (nameValidationResult && parameterValidationResult)
    },
    deleteTask: function () {
      this.showDeleteDialog = false
      this.$emit('delete')
    }
  },
  mounted () {
    this.loadProviderTypes()
  }
}
</script>

<style scoped>

  .provider-parameters {
    margin-top: 25px;
  }

</style>
