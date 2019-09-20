<template>
    <div class="sticky max-width">
        <core-panel>
            <h1 class="truncate">
                <font-awesome-icon icon="wrench"/>
                {{ action.type.label }}
            </h1>

            <table>
                <tr>
                    <td><label>Active</label></td>
                    <td>
                        <font-awesome-icon :icon="action.active ? 'check-square' : 'square'"
                                           v-on:click="action.active = !action.active"/>
                    </td>
                    <td/>
                </tr>
                <tr>
                    <td><label>Category</label></td>
                    <td>
                        <select v-model="action.category" v-on:change="loadTypesOfCategory(action.category.key, true).then(() => {
                                        loadParametersOfType(action.type.key)})">
                            <option v-for="category in actionCategories" v-bind:value="category"
                                    v-bind:key="category.key">
                                {{category.label}}
                            </option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td>Type</td>
                    <td>
                        <select v-model="action.type" v-on:change="loadParametersOfType(action.type.key)">
                            <option v-for="type in actionTypes" v-bind:value="type"
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
            <parameter-editor :parameters="action.parameters" ref="parameterEditor"
                v-on:create-service="createService"/>
        </core-panel>
    </div>
</template>

<script>
  import CorePanel from '../common/core-panel'
  import ParameterEditor from '../common/parameter-editor'
  import IgorBackend from '../../utils/igor-backend.js'

  export default {
  name: 'action-configurator',
  components: {CorePanel, ParameterEditor},
  props: ['action', 'actionKey'],
  data: function () {
    return {
      actionCategories: [],
      actionTypes: []
    }
  },
  methods: {
    loadCategories: async function () {
      await IgorBackend.getData('/api/category/action').then((categories) => {
        for (let i = this.actionCategories.length; i > 0; i--) {
          this.actionCategories.pop()
        }
        let component = this
        Array.from(categories).forEach(function (item) {
          component.actionCategories.push(item)
        })
      })
    },
    loadTypesOfCategory: async function (categoryKey, selectFirst) {
      await IgorBackend.getData('/api/type/action/' + categoryKey).then((types) => {
        for (let i = this.actionTypes.length; i > 0; i--) {
          this.actionTypes.pop()
        }
        let component = this
        Array.from(types).forEach(function (item) {
          component.actionTypes.push(item)
        })
        if (selectFirst) {
          this.action.type = this.actionTypes[0]
        }
      })
    },
    loadParametersOfType: function (typeKey) {
      IgorBackend.getData('/api/parameters/action/' + typeKey).then((parameters) => {
        this.action.parameters = parameters
      })
    },
    validateInput: function () {
      let parameterValidationResult = true
      if (typeof this.$refs.parameterEditor !== 'undefined') {
        parameterValidationResult = this.$refs.parameterEditor.validateInput()
      }
      return parameterValidationResult
    },
    createService: function (parameterIndex, serviceCategory) {
      this.$emit('create-service', this.actionKey, parameterIndex, serviceCategory)
    }
  },
  watch: {
    action: function() {
      // When an action is moved in the tree-navigation, the vue-model changes for the component!
      this.loadCategories().then(() => {
        this.loadTypesOfCategory(this.action.category.key, false)
      })
    }
  },
  mounted: function () {
    this.loadCategories().then(() => {
      this.loadTypesOfCategory(this.action.category.key, false).then(() => {
        // Don't load type parameters as they are already provided within the component's model:
        if (!(Array.isArray(this.action.parameters) && this.action.parameters.length)) {
          this.loadParametersOfType(this.action.type.key)
        }
      })
    })
  }
}
</script>

<style scoped>

</style>
