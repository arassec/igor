<template>
    <div>
        <core-panel>

            <test-result-marker v-if="showTestResultMarker()" v-on:clicked="$emit('show-action-test-results')"/>

            <h1>
                <font-awesome-icon icon="wrench"/>
                {{ action.label }}
            </h1>

            <table>
                <tr>
                    <td>Type</td>
                    <td>
                        <select v-model="action.type" v-on:change="loadTypeParameters()">
                            <option v-for="actionType in actionTypes" v-bind:value="actionType.type"
                                    v-bind:key="actionType.type">
                                {{actionType.label}}
                            </option>
                        </select>
                    </td>
                </tr>
            </table>
        </core-panel>

        <core-panel>
            <h2>Action Parameters</h2>
            <parameter-editor :parameters="action.parameters" ref="parameterEditor"/>
        </core-panel>
    </div>
</template>

<script>
import CorePanel from '../common/core-panel'
import ParameterEditor from '../common/parameter-editor'

export default {
  name: 'action-configurator',
  components: {ParameterEditor, CorePanel},
  props: ['action', 'actionTypes'],
  watch: {
    action: function() {
      this.loadTypeParameters()
    }
  },
  methods: {
    loadTypeParameters: function () {
      for (let i = 0; i < this.actionTypes.length; i++) {
        if (this.actionTypes[i].type === this.action.type) {
          this.action.label = this.actionTypes[i].label
        }
      }
      let component = this
      this.$http.get('/api/actionparams/' + this.action.type).then(function (response) {
        component.action.parameters = response.data
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
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
  mounted: function() {
    this.loadTypeParameters()
  }
}
</script>

<style scoped>

</style>
