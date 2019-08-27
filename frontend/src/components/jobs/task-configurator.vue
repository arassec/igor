<template>
    <div class="sticky max-width">
        <core-panel>
            <h1 class="truncate">
                <font-awesome-icon icon="tasks"/>
                {{ task.name.length > 0 ? task.name : 'Unnamed Task' }}
            </h1>

            <table>
                <tr>
                    <td><label>Active</label></td>
                    <td>
                        <font-awesome-icon :icon="task.active ? 'check-square' : 'square'"
                                           v-on:click="task.active = !task.active"/>
                    </td>
                    <td/>
                </tr>
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
                    <td><label>Dry-Run limit</label></td>
                    <td>
                        <input type="text" autocomplete="off" v-model.number="task.dryrunLimit"/>
                    </td>
                    <td/>
                </tr>
            </table>
        </core-panel>

        <core-panel>
            <h2>Provider</h2>
            <table>
                <tr v-if="providerCategories.length > 1">
                    <td><label>Category</label></td>
                    <td>
                        <select v-model="task.provider.category"
                                v-on:change="loadTypesOfCategory(task.provider.category.key, true).then(() => {
                                        loadParametersOfType(task.provider.type.key)})">
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
                        <select v-model="task.provider.type" v-on:change="loadParametersOfType(task.provider.type.key)">
                            <option v-for="providerType in providerTypes" v-bind:value="providerType"
                                    v-bind:key="providerType.key">
                                {{providerType.label}}
                            </option>
                        </select>
                    </td>
                </tr>
            </table>
        </core-panel>

        <core-panel v-if="task.provider.parameters.length">
            <h2>Provider Parameters</h2>
            <parameter-editor v-bind:parameters="task.provider.parameters" ref="parameterEditor"
                              v-on:create-service="createService"/>
        </core-panel>

    </div>
</template>

<script>
  import ValidationError from '../common/validation-error'
  import ParameterEditor from '../common/parameter-editor'
  import CorePanel from '../common/core-panel'
  import IgorBackend from '../../utils/igor-backend.js'

  export default {
  name: 'task-configurator',
  components: {CorePanel, ParameterEditor, ValidationError},
  props: ['task', 'taskKey'],
  data: function () {
    return {
      nameValidationError: '',
      providerCategories: [],
      providerTypes: []
    }
  },
  methods: {
    loadCategories: async function () {
      await IgorBackend.getData('/api/category/provider').then((categories) => {
        for (let i = this.providerCategories.length; i > 0; i--) {
          this.providerCategories.pop()
        }
        let component = this
        Array.from(categories).forEach(function (item) {
          component.providerCategories.push(item)
        })
      })
    },
    loadTypesOfCategory: async function (categoryKey, selectFirst) {
      await IgorBackend.getData('/api/type/provider/' + categoryKey).then((types) => {
        for (let i = this.providerTypes.length; i > 0; i--) {
          this.providerTypes.pop()
        }
        let component = this
        Array.from(types).forEach(function (item) {
          component.providerTypes.push(item)
        })
        if (selectFirst) {
          this.task.provider.type = this.providerTypes[0]
        }
      })
    },
    loadParametersOfType: function (typeKey) {
      IgorBackend.getData('/api/parameters/provider/' + typeKey).then((parameters) => {
        this.task.provider.parameters = parameters
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
    createService: function (parameterIndex, serviceCategory) {
      this.$emit('create-service', this.taskKey, parameterIndex, serviceCategory)
    }
  },
  watch: {
    task: function() {
      // When an action is moved in the tree-navigation, the vue-model changes for the component!
      this.loadCategories().then(() => {
        this.loadTypesOfCategory(this.task.provider.category.key, false)
      })
    }
  },
  mounted () {
    this.loadCategories().then(() => {
      this.loadTypesOfCategory(this.task.provider.category.key, false).then(() => {
        // Don't load type parameters if they are provided within the component's model:
        if (!(Array.isArray(this.task.provider.parameters) && this.task.provider.parameters.length)) {
          this.loadParametersOfType(this.task.provider.type.key)
        }
      })
    })
  }
}
</script>

<style scoped>

</style>
