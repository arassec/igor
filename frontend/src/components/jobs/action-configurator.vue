<template>
    <div>
        <core-panel>
            <h1>
                <font-awesome-icon icon="wrench"/>
                {{ action.type.label }}
            </h1>

            <table>
                <tr>
                    <td><label>Category</label></td>
                    <td>
                        <select v-model="action.category" v-on:change="loadTypes(action.category.key)">
                            <option v-for="category in categories" v-bind:value="category"
                                    v-bind:key="category.key">
                                {{category.label}}
                            </option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td>Type</td>
                    <td>
                        <select v-model="action.type" v-on:change="loadTypeParameters(action.type.key)">
                            <option v-for="type in types" v-bind:value="type"
                                    v-bind:key="type.key">
                                {{type.label}}
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
  components: {CorePanel, ParameterEditor},
  props: ['action', 'actionKey'],
  data: function () {
    return {
      categories: [],
      types: [],
      initializeTypeParameters: true
    }
  },
  methods: {
    loadCategories: function () {
      let component = this
      this.$http.get('/api/category/action').then(function (response) {
        for (let i = component.categories.length; i > 0; i--) {
          component.categories.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.categories.push(item)
        })
        component.loadTypes(component.action.category.key)
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    loadTypes: function (categoryType) {
      let component = this
      this.$http.get('/api/type/action/' + categoryType).then(function (response) {
        for (let i = component.types.length; i > 0; i--) {
          component.types.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.types.push(item)
        })
        // Skip the parameter loading if they already exist in the model:
        if (component.initializeTypeParameters) {
          component.loadTypeParameters(component.action.type.key)
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
      this.$http.get('/api/parameters/action/' + type).then(function (response) {
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
      return parameterValidationResult
    }
  },
  watch: {
    action: function() {
      // When an action is moved in the tree-navigation, the model changes for the component!
      this.initializeTypeParameters = false
      this.loadCategories()
    }
  },
  mounted: function () {
    // Don't load type parameters as they are already provided within the component's model:
    if (Array.isArray(this.action.parameters) && this.action.parameters.length) {
      this.initializeTypeParameters = false
    }
    this.loadCategories()
  }
}
</script>

<style scoped>

</style>
