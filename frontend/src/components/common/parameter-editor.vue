<template>
  <div>

    <table>
      <template v-for="(param, index) in parameters" v-bind:id="param.name" v-bind:index="index">
        <tr v-bind:key="param.name" v-show="(showAdvancedParameters && isAdvancedParameter(param)) || !isAdvancedParameter(param)">
          <td class="text-top">
            <label v-if="param.optional">{{formatParameterName(param.name)}}</label>
            <label v-if="!param.optional">{{formatParameterName(param.name)}}*</label>
          </td>
          <td>
            <input v-if="isNumber(param.type)" type="text" autocomplete="off"
                   v-model.number="param.value"/>
            <font-awesome-icon v-else-if="isBoolean(param.type)" :icon="param.value ? 'check-square' : 'square'"
                               v-on:click="param.value = !param.value"/>
            <input v-else-if="isService(param)" :disabled="true" v-model="param.serviceName"/>
            <textarea v-else-if="param.subtype === 'MULTI_LINE'" v-model="param.value" rows="8" cols="41" />
            <input v-else autocomplete="off"
                   :type="parameterInputTypes[index]" v-model.trim="param.value"/>

            <input-button v-if="!isNumber(param.type) && !isBoolean(param.type) && param.secured"
                          icon="eye" v-on:clicked="toggleCleartext(index)" class="button-margin-left"/>
            <input-button v-else-if="isService(param)" icon="cogs" v-on:clicked="openServicePicker(index, param.type)" class="button-margin-left"/>
            <input-button v-else-if="param.subtype === 'CRON'" v-on:clicked="openCronPicker(index)" icon="clock" class="button-margin-left"/>
          </td>
          <td>
            <validation-error v-if="checkValidationError(index)">
              {{parameterValidationErrors[index]}}
            </validation-error>
          </td>
        </tr>
      </template>
      <tr v-if="advancedParametersExist()">
        <td colspan="2">
          <font-awesome-icon class="arrow" v-bind:icon="showAdvancedParameters ? 'chevron-up' : 'chevron-down'"
                             v-on:click="showAdvancedParameters = !showAdvancedParameters"/>
        </td>
      </tr>
    </table>

    <service-picker v-show="showServicePicker" :services="services"
                    v-on:cancel="showServicePicker = false" v-on:create="createService" v-on:selected="setSelectedService"/>

    <cron-picker v-show="showCronPicker" v-on:selected="setCronExpression" v-on:cancel="showCronPicker = false"/>

  </div>
</template>

<script>
import ValidationError from './validation-error'
import InputButton from './input-button'
import ServicePicker from '../services/service-picker'
import CronPicker from "./cron-picker";

export default {
  name: 'parameter-editor',
  components: {CronPicker, ServicePicker, InputButton, ValidationError},
  props: ['parameters'],
  data: function () {
    return {
      validationOk: true,
      showServicePicker: false,
      showCronPicker: false,
      showAdvancedParameters: false,
      serviceParameterIndex: 0,
      serviceParameterCategory: null,
      cronParameterIndex: 0,
      parameterValidationErrors: [],
      parameterInputTypes: [],
      services: []
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
    isService: function (parameter) {
      if (parameter.service) {
        return true
      }
      return false
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
    },
    openServicePicker: function (index, serviceCategory) {
      let component = this
      component.serviceParameterCategory = serviceCategory
      this.$http.get('/api/service/category/' + serviceCategory).then(function (response) {
        for (let i = component.services.length; i > 0; i--) {
          component.services.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.services.push(item)
        })
        component.services.sort((a, b) => a.name.localeCompare(b.name))
        component.serviceParameterIndex = index
        component.showServicePicker = true
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Services could not be loaded: ' + error, true)
      })
    },
    setSelectedService: function (service) {
      this.parameters[this.serviceParameterIndex].serviceName = service.name
      this.parameters[this.serviceParameterIndex].value = service.id
      this.showServicePicker = false
    },
    createService: function () {
      this.$emit('create-service', this.serviceParameterIndex, this.serviceParameterCategory)
    },
    openCronPicker: function (index) {
      this.cronParameterIndex = index
      this.showCronPicker = true
    },
    setCronExpression: function (value) {
      this.parameters[this.cronParameterIndex].value = value
      this.showCronPicker = false
    },
    isAdvancedParameter: function (parameter) {
      if (parameter.name === 'dataKey' || parameter.name === 'numThreads' || parameter.name === 'directoryKey') {
          return true
      }
      return parameter.optional
    },
    advancedParametersExist: function () {
      for (let index = 0; index < this.parameters.length; index++) {
        if (this.isAdvancedParameter(this.parameters[index])) {
          return true
        }
      }
      return false
    }
  },
  created: function () {
    let component = this
    for (let i in this.parameters) {
      let param = this.parameters[i]
      if (!component.isNumber(param.type) && !component.isBoolean(param.type)) {
        if (param.secured) {
          component.$set(component.parameterInputTypes, i, 'password')
        } else {
          component.$set(component.parameterInputTypes, i, 'text')
        }
      }
    }
  }
}
</script>

<style scoped>

  .arrow:hover {
    cursor: pointer;
  }

  .text-top {
    vertical-align: top;
  }

  textarea {
    color: var(--font-color-light);
    background-color: var(--element-background-color);
    border: none;
  }

</style>
