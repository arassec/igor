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

            </table>
        </core-panel>

        <core-panel>
            <h2>Provider</h2>
            <table>
                <tr>
                    <td><label>Category</label></td>
                    <td>
                        <select v-model="task.provider.category"
                                v-on:change="loadTypes(task.provider.category.key)">
                            <option v-for="providerCategory in providerCategories" v-bind:value="providerCategory"
                                    v-bind:key="providerCategory.key">
                                {{providerCategory.label}}
                            </option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><label>Type</label></td>
                    <td>
                        <select v-model="task.provider.type" v-on:change="loadTypeParameters(task.provider.type.key)">
                            <option v-for="providerType in providerTypes" v-bind:value="providerType"
                                    v-bind:key="providerType.key">
                                {{providerType.label}}
                            </option>
                        </select>
                    </td>
                </tr>
            </table>
        </core-panel>

        <core-panel>
            <h2>Provider Parameters</h2>
            <parameter-editor v-bind:parameters="task.provider.parameters" ref="parameterEditor"/>
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
      providerCategories: [],
      providerTypes: [],
      initializeTypeParameters: true
    }
  },
  methods: {
    loadCategories: function () {
      let component = this
      this.$http.get('/api/category/provider').then(function (response) {
        for (let i = component.providerCategories.length; i > 0; i--) {
          component.providerCategories.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.providerCategories.push(item)
        })
        component.loadTypes(component.task.provider.category.key)
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    loadTypes: function (categoryType) {
      let component = this
      this.$http.get('/api/type/provider/' + categoryType).then(function (response) {
        for (let i = component.providerTypes.length; i > 0; i--) {
          component.providerTypes.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.providerTypes.push(item)
        })
        // Skip the parameter loading if they already exist in the model:
        if (component.initializeTypeParameters) {
          component.task.provider.type = component.providerTypes[0]
          component.loadTypeParameters(component.task.provider.type.key)
        } else {
          // After the first skip, always load parameters:
          component.initializeTypeParameters = true
        }
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    loadTypeParameters: function (type) {
      let component = this
      component.initializeTypeParameters = true
      this.$http.get('/api/parameters/provider/' + type).then(function (response) {
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
    // Don't load type parameters as they are already provided within the component's model:
    if (Array.isArray(this.task.provider.parameters) && this.task.provider.parameters.length) {
      this.initializeTypeParameters = false
    }
    this.loadCategories()
  }
}
</script>

<style scoped>

</style>
