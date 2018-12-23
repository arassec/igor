<template>
    <div>
        <core-panel>
            <h1>
                <font-awesome-icon icon="tasks"/>
                {{ task.name.length > 0 ? task.name : 'Unnamed Task' }}
            </h1>

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
        </core-panel>

        <core-panel>
            <h2>Provider Parameters</h2>
            <parameter-editor :parameters="task.provider.parameters" ref="parameterEditor"/>
        </core-panel>

    </div>
</template>

<script>
import ValidationError from '../common/validation-error'
import ParameterEditor from '../common/parameter-editor'
import CorePanel from '../common/core-panel'

export default {
  name: 'task-configurator',
  components: {CorePanel, ParameterEditor, ValidationError},
  props: ['task'],
  data: function () {
    return {
      nameValidationError: '',
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

      this.$forceUpdate()

      return (nameValidationResult && parameterValidationResult)
    }
  },
  mounted () {
    this.loadProviderTypes()
  }
}
</script>

<style scoped>

</style>
