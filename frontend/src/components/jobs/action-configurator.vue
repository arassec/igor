<template>
    <div>
        <core-panel>
            <h1>
                <font-awesome-icon icon="wrench"/>
                {{ action.label }}
            </h1>

            <table>
                <tr>
                    <td>Type</td>
                    <td>
                        <select v-model="action.type" v-on:change="loadTypeParameters(true)">
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
            <parameter-editor v-bind:parameters="action.parameters" ref="parameterEditor"/>
        </core-panel>
    </div>
</template>

<script>
import CorePanel from '../common/core-panel'
import ParameterEditor from '../common/parameter-editor'

export default {
  name: 'action-configurator',
  components: {ParameterEditor, CorePanel},
  props: ['action', 'actionTypes', 'actionKey'],
  methods: {
    loadTypeParameters: function (forceParameterReload) {
      for (let i = 0; i < this.actionTypes.length; i++) {
        if (this.actionTypes[i].type === this.action.type) {
          this.action.label = this.actionTypes[i].label
        }
      }
      if (Object.keys(this.action.parameters).length === 0 || forceParameterReload) {
        let component = this
        this.$http.get('/api/actionparams/' + this.action.type).then(function (response) {
          component.action.parameters = response.data
        }).catch(function (error) {
          component.feedback = error
          component.feedbackOk = false
        })
      }
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
  mounted: function() {
    this.loadTypeParameters(false)
  }
}
</script>

<style scoped>

</style>
