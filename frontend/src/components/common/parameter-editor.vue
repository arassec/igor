<template>
    <table>
        <template v-for="(param, index) in parameters" v-bind:id="param.name" v-bind:index="index">
            <tr v-bind:key="param.name">
                <td>
                    <label v-if="param.optional">{{formatParameterName(param.name)}}</label>
                    <label v-if="!param.optional">{{formatParameterName(param.name)}}*</label>
                </td>
                <td>
                    <input v-if="isNumber(param.type)" type="text" autocomplete="off"
                           v-model.number="param.value"/>

                    <font-awesome-icon v-if="isBoolean(param.type)" :icon="param.value ? 'check-square' : 'square'"
                                       v-on:click="param.value = !param.value"/>

                    <input v-if="!isNumber(param.type) && !isBoolean(param.type)" autocomplete="off"
                           :type="parameterInputTypes[index]" v-model.trim="param.value"/>

                    <font-awesome-icon icon="eye" class="cleartext-icon"
                        v-if="!isNumber(param.type) && !isBoolean(param.type) && param.secured"
                                       v-on:click="toggleCleartext(index)"/>

                </td>
                <td>
                    <validation-error v-if="checkValidationError(index)">
                        {{parameterValidationErrors[index]}}
                    </validation-error>
                </td>
            </tr>
        </template>
    </table>
</template>

<script>
import ValidationError from './validation-error'
import InputButton from './input-button'

export default {
  name: 'parameter-editor',
  components: {InputButton, ValidationError},
  props: ['parameters'],
  data: function () {
    return {
      validationOk: true,
      parameterValidationErrors: [],
      parameterInputTypes: []
    }
  },
  events: {
    'validate-input': function () {
      this.validateInput()
    }
  },
  methods: {
    isNumber: function (parameter) {
      let result = (parameter === 'int' ||
                    parameter === 'java.lang.Integer' ||
                    parameter === 'long' ||
                    parameter === 'java.lang.Long')
      return result
    },
    isBoolean: function (parameter) {
      let result = (parameter === 'boolean' ||
                    parameter === 'java.lang.Boolean')
      return result
    },
    formatParameterName: function (string) {
      string = string.replace(/\.?([A-Z])/g, function (x, y) {
        return ' ' + y.toLowerCase()
      }).replace(/^_/, '')
      return string.charAt(0).toUpperCase() + string.slice(1)
    },
    validateInput: function () {
      for (let i = this.parameterValidationErrors.length; i > 0; i--) {
        this.parameterValidationErrors[i] = ''
      }
      let component = this

      this.parameters.forEach(function (param, index) {
        component.parameterValidationErrors[index] = ''
      })

      this.parameters.forEach(function (param, index) {
        if (param.optional && param.value == null) {
          param.value = ''
        }
        if (!param.optional && (param.value == null || param.value === '')) {
          component.parameterValidationErrors[index] = 'Value required'
          component.validationOk = false
        }
      })

      this.$forceUpdate()

      if (!component.validationOk) {
        component.validationOk = true
        return false
      }

      return true
    },
    checkValidationError: function (index) {
      if (typeof this.parameterValidationErrors[index] === 'undefined') {
        this.parameterValidationErrors[index] = ''
      }
      return this.parameterValidationErrors[index].length > 0
    },
    toggleCleartext: function (index) {
      if (this.parameterInputTypes[index] === 'password') {
        this.$set(this.parameterInputTypes, index, 'text')
      } else {
        this.$set(this.parameterInputTypes, index, 'password')
      }
    }
  },
  created: function () {
    let component = this
    this.parameters.forEach(function (param, index) {
      if (!component.isNumber(param.type) && !component.isBoolean(param.type)) {
        if (param.secured) {
          component.$set(component.parameterInputTypes, index, 'password')
        } else {
          component.$set(component.parameterInputTypes, index, 'text')
        }
      }
    })
  }
}
</script>

<style scoped>

    table tr {
        line-height: 30px;
    }

    table td {
        text-align: left;
        padding-right: 15px;
    }

    table tr td label, svg {
        color: var(--font-color-light);
        margin-right: 15px;
    }

    table tr td input, select {
        border: none;
        background-color: var(--element-background-color);
        height: 25px;
        min-width: 300px;
        color: var(--font-color-light);
        padding-left: 5px;
    }

    table tr td select option {
        border: none;
        outline: none;
        background-color: var(--element-background-color);
        height: 25px;
    }

    table tr td select:focus,
    table tr td input:focus {
        background-color: var(--element-background-color-focus);
        color: var(--panel-background-color);
        outline: none;
    }

    .cleartext-icon {
        margin: 0px 0px -2px 5px;
    }

    .cleartext-icon:hover {
        color: var(--font-color-dark);
        cursor: pointer;
    }

</style>
