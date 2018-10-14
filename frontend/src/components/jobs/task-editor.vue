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
                        <select v-model="selectedProvider" v-on:change="loadTypeParameters(selectedProvider)">
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
                    <input-button class="right" v-on:clicked="addAction()" icon="plus"/>
                </p>
            </button-row>

        </core-panel>
    </div>
</template>

<script>
import CorePanel from '../common/core-panel'
import InputButton from '../common/input-button'
import ParameterEditor from '../common/parameter-editor'
import ButtonRow from '../common/button-row'

export default {
  name: 'task-editor',
  components: {ButtonRow, ParameterEditor, CorePanel, InputButton},
  props: ['task'],
  data: function () {
    return {
      feedback: '',
      feedbackOk: true,
      requestInProgress: false,
      providerTypes: [],
      selectedProvider: ''
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
        component.selectedProvider = component.providerTypes[0].type
        console.log('PROVIDER: ' + component.selectedProvider)
        component.loadTypeParameters(component.selectedProvider)
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
