<template>
  <core-panel class="action-editor">

    <table>
      <tr>
        <td><h1>Action</h1></td>
        <td>
          <select v-model="action.type" v-on:change="loadTypeParameters(action.type)">
            <option v-for="actionType in actionTypes" v-bind:value="actionType.type"
                    v-bind:key="actionType.type">
              {{actionType.label}}
            </option>
          </select>
        </td>
      </tr>
    </table>

    <div class="action-parameters">
      <h3>Action Parameters</h3>
      <parameter-editor :parameters="action.parameters" ref="parameterEditor"/>
    </div>

  </core-panel>
</template>

<script>
import ParameterEditor from '../common/parameter-editor'
import CorePanel from '../common/core-panel'

export default {
  name: 'action-editor',
  components: {CorePanel, ParameterEditor},
  props: ['action'],
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
          component.loadTypeParameters(component.action.type)
        }
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    loadTypeParameters: function (type) {
      let component = this
      this.$http.get('/api/actionparams/' + type).then(function (response) {
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
    }
  },
  mounted () {
    this.loadActionTypes()
  }
}
</script>

<style scoped>

  .action-editor {
    margin-left: 25px;
  }

  .action-parameters {
    margin-top: 25px;
  }

  table tr td h1 {
    margin-bottom: 0px;
  }
</style>
